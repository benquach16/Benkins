import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.net.*;
import java.io.*;
import java.lang.StringBuilder;


class GitScrape {
    public static void main(String[] args) {
		try{
			URL url = new URL("https://api.github.com/repos/dylanjay/cs183proj/pulls");
			URLConnection con = url.openConnection();
			con.setRequestProperty("Accept", "application/vnd.github.full+json");


			BufferedReader in = new BufferedReader(new InputStreamReader(
                                    con.getInputStream()));
			StringBuilder strBuild = new StringBuilder();
			String line = "";
			while((line = in.readLine()) != null){
				strBuild.append(line);
			}
			GetPullRequestInfo(strBuild.toString());
		}
		catch (Exception e){
			e.printStackTrace();
		}
	
	}

	public static void GetPullRequestInfo(String jsonAsString){
		Gson gson = new GsonBuilder().create();
		JsonArray pullRequestJArray = gson.fromJson(jsonAsString, JsonArray.class);
		for(int i = 0; i < pullRequestJArray.size(); i++)
		{
			String pullReqNumber = pullRequestJArray.get(i).getAsJsonObject().get("number").getAsString();
			String repoURL = pullRequestJArray.get(i).getAsJsonObject().get("head").getAsJsonObject().get("repo").getAsJsonObject().get("clone_url").getAsString();
			spawnVM(repoURL, pullReqNumber);
		}
	}

	public static void spawnVM(String repoURL, String pullRequestNumber)
	{
		System.out.println(repoURL + ", " + pullRequestNumber);
		//To get the pull request:
		//<pqr> = Pull Request Number
		//git fetch origin pull/<pqr>/head:pr-<pqr>
		//git checkout pr--<pqr>
	}
	
}
