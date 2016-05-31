import java.util.ArrayList;
import com.google.gson.*;
import java.net.*;
import java.io.*;
import java.lang.StringBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Hashtable;
import java.util.LinkedList;
import java.lang.ProcessBuilder;
import java.util.Scanner;

import java.util.Queue;

import com.google.gson.TypeAdapter;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

class GitScrape {

	public static Map<String, JsonObject> allJobs = new Hashtable<String, JsonObject>();
	public static Queue<String> queuedJobs = new LinkedList<String>();

    public static void main(String[] args) {

		//If the program is killed, we will want to write all the jobs to a file.
		//This way we won't start up additional VMs to run pull requests that have
		//already been handled.
		Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run()
            {
                writeAllJobsToFile();
            }
        });

		System.out.println("Working Directory = " + System.getProperty("user.dir"));


		//Read from the jobs (if there are any written from previous runs)
		readAllJobsFromFile();


		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

		Runnable periodicTask = new Runnable() {
			public void run() {
				System.out.println("Updating List...");

				ArrayList<String> repoList = getAllRepositories();
				checkForPullRequests(repoList);

				System.out.println("Update Complete. Checking the Queue...");
				
				while(!queuedJobs.isEmpty()){
					spawnVM(queuedJobs.poll());
				}

				System.out.println("Queue Check Complete");
			}
		};

		executor.scheduleAtFixedRate(periodicTask, 0, 1, TimeUnit.MINUTES);
		
	}

	//Writes all jobs to a file
	public static void writeAllJobsToFile(){
		String filename = "../resources/pullRequestsList.txt";
		
		try (Writer writer = new FileWriter(filename)) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(allJobs, writer);
    	}catch(Exception e){System.err.println("Failed to pending jobs to record.");}
	}

	//Reads all jobs from a file. Clears current jobs.
	//Should only be used for initialization.
	public static void readAllJobsFromFile(){
		String filename = "../resources/pullRequestsList.txt";

		String jsonAsString = "";
		File f = new File(filename);
		try{
			if(f.exists() && !f.isDirectory()) {
				jsonAsString = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
				Type type = new TypeToken<Map<String, JsonObject>>(){}.getType();
				Gson gson = new GsonBuilder().create();
				allJobs = gson.fromJson(jsonAsString, type);
			}
			else{
				System.err.println("Failed to locate "+filename);
			}
		}catch (Exception e){System.err.println("Failed to read from "+filename); e.printStackTrace();}
		
	}

	//Gets all repositories from a file that are supposed to be tracked
	//by this program.
	//Returns the ArrayList of the repository names.
	public static ArrayList<String> getAllRepositories(){
		String filename = "../resources/listOfRepositories.txt";
		String repoNames = "";
		File f = new File(filename);
		try{
			if(f.exists() && !f.isDirectory()) {
				repoNames = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
				//repoNames = repoNames.substring(0, repoNames.length()-1);
			}
			else{
				System.err.println("Failed to locate "+filename);
			}
		}catch (Exception e){System.err.println("Failed to read from "+filename);}
		return new ArrayList<String>(java.util.Arrays.asList(repoNames.split("\n")));
	}

	//Checks all of the repositories for open pull requests. If a pull request
	//Has already been run by this program or is closed, it will ignore those
	//requests. Stores the pull requests in 
	public static void checkForPullRequests(ArrayList<String> repoList){
		for(int i = 0; i < repoList.size(); i++){
			System.out.println("Checking pull requests for " + repoList.get(i));
			
			try{
				URL url = new URL("https://api.github.com/repos/" + repoList.get(i).replaceAll("\\s+","") + "/pulls");
				URLConnection con = url.openConnection();
				con.setRequestProperty("Accept", "application/vnd.github.full+json");

				BufferedReader in = new BufferedReader(new InputStreamReader(
		                                con.getInputStream()));
				StringBuilder strBuild = new StringBuilder();
				String line = "";
				while((line = in.readLine()) != null){
					strBuild.append(line);
				}
				getPullRequestInfo(strBuild.toString());
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	//Pulls some of the relevant values and passes them into SpawnVM
	public static void getPullRequestInfo(String jsonAsString){
		Gson gson = new GsonBuilder().create();
		JsonArray pullRequestJArray = gson.fromJson(jsonAsString, JsonArray.class);
		if(pullRequestJArray.size() == 0){System.out.println("No pull requests found.");}
		for(int i = 0; i < pullRequestJArray.size(); i++)
		{
			String pullReqURL = pullRequestJArray.get(i).getAsJsonObject().get("url").getAsString();
			if(!allJobs.containsKey(pullReqURL)){
				
				allJobs.put(pullReqURL, pullRequestJArray.get(i).getAsJsonObject());
				queuedJobs.add(pullReqURL);
			}
			else{
				System.out.println("Pull request " + 
					pullRequestJArray.get(i).getAsJsonObject().get("number").getAsString() + 
					" already tested.");
			}
		}
	}

	public static void spawnVM(String pullReqURL){
		System.out.println("Spawning...");
		//To get the pull request:
		//<pqr> = Pull Request Number
		//git fetch origin pull/<pqr>/head:pr-<pqr>
		//git checkout pr--<pqr>
		
		//Get All Relevant Information
		JsonObject pullReqInfo = allJobs.get(pullReqURL);
		String pullReqNumber = pullReqInfo.get("number").getAsString();
		String repoName = getString(pullReqInfo, "head:repo:name");
		String handle = getString(pullReqInfo, "head:user:login");
		String branchName = getString(pullReqInfo, "head:ref");
		
		//Overwrite the test scripts to take the correct repo
		try{
			Path path = Paths.get("../scripts/runTests.sh");
			String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			System.out.println("Setting Handle");
			content = content.replaceFirst("HANDLE=.+?#__HANDLE__",
											"HANDLE=\"" + handle + "\" #__HANDLE__");
			System.out.println("Setting Repo Name");
			content = content.replaceFirst("REPO=.+?#__REPO__", 
											"REPO=\"" + repoName + "\" #__REPO__");
			System.out.println("Setting Branch Name");
			content = content.replaceFirst("BRANCH=.+?#__BRANCH__",
											"BRANCH=\"" + branchName + "\" #__BRANCH__");
			System.out.println("Setting PullReqNum");
			content = content.replaceFirst("PULL_REQ=.+?#__PULL_REQ__",
											"PULL_REQ=\"" + pullReqNumber + "\" #__PULL_REQ__");
			Files.write(path, content.getBytes(StandardCharsets.UTF_8));
		}
		catch(Exception e){e.printStackTrace();}
		
		
		
		//Run "vagrant up". Wait for it to complete before continuing.
		try{
			Process p = Runtime.getRuntime().exec("vagrant up");
			inheritIO(p.getInputStream(), System.out);
			inheritIO(p.getErrorStream(), System.err);
			p.waitFor();
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	private static void inheritIO(final InputStream src, final PrintStream dest) {
		new Thread(new Runnable() {
			public void run() {
				Scanner sc = new Scanner(src);
				while (sc.hasNextLine()) {
					dest.println(sc.nextLine());
				}
			}
		}).start();
	}

	public static String getString(JsonElement jsonElement, String[] path, int index){
		try{
			if(index >= path.length){
				return jsonElement.getAsString();
			}
			
			int braceIndex = path[index].indexOf("[");
			if(braceIndex == 0){
				int arrayIndex = Integer.parseInt(path[index].substring(braceIndex, path[index].length()-1));
				return getString(jsonElement.getAsJsonArray().get(arrayIndex), path, ++index);
			}
			else if(braceIndex > 0){
				String arrayName = path[index].substring(0, braceIndex);
				int arrayIndex = Integer.parseInt(path[index].substring(braceIndex, path[index].length()-1));
				return getString(jsonElement.getAsJsonObject().get(arrayName).getAsJsonArray().get(arrayIndex), path, ++index);
			}
			else{
				return getString(jsonElement.getAsJsonObject().get(path[index]), path, ++index);
			}
		}catch(Exception e){e.printStackTrace(); return "";}
		
	}

	public static String getString(JsonElement jsonElement, String path){
		return getString(jsonElement, path.split(":"), 0);
	}
}
