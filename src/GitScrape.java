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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Hashtable;
import java.util.LinkedList;

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
		Runtime.getRuntime().addShutdownHook(new Thread()
        {
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

				if(!queuedJobs.isEmpty()){
					spawnVM(queuedJobs.poll());
				}

				System.out.println("Queue Check Complete");
			}
		};

		executor.scheduleAtFixedRate(periodicTask, 0, 5, TimeUnit.MINUTES);
		
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
			try{
				URL url = new URL("https://api.github.com/repos/" + repoList.get(i) + "/pulls");
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
		for(int i = 0; i < pullRequestJArray.size(); i++)
		{
			String pullReqNumber = pullRequestJArray.get(i).getAsJsonObject().get("number").getAsString();
			String repoURL = getString(pullRequestJArray.get(i), "head:repo:clone_url");
			String branchName = getString(pullRequestJArray.get(i), "head:ref");
			String pullReqURL = pullRequestJArray.get(i).getAsJsonObject().get("url").getAsString();
			System.out.println("RepoURL: " + repoURL);
			System.out.println("branchName: " + branchName);
			if(!allJobs.containsKey(pullReqURL)){
				
				allJobs.put(pullReqURL, pullRequestJArray.get(i).getAsJsonObject());
				queuedJobs.add(pullReqURL);
			}
		}
	}

	public static void spawnVM(String pullReqURL)
	{
		System.out.println(pullReqURL);
		//To get the pull request:
		//<pqr> = Pull Request Number
		//git fetch origin pull/<pqr>/head:pr-<pqr>
		//git checkout pr--<pqr>
		try{
			//String[] cmd = new String[]{"/bin/sh", "/home/ben/IntegrationApp/src/bash/vm.sh"};

			Process pr = Runtime.getRuntime().exec("vagrant up");
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String line = "";

			while ((line=buf.readLine())!=null) {

				System.out.println(line);

			}
			System.out.print("sdf");
		}
		catch(Exception e){}
	}

	public static String getString(JsonElement jsonElement, String[] path, int index){
		if(index == path.length){
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
	}

	public static String getString(JsonElement jsonElement, String path){
		return getString(jsonElement, path.split(":"), 0);
	}
}
