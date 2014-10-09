package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.narrativemethodstore.db.MethodSpecDB;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;




public class GitHubDB implements MethodSpecDB {

	public static final String GITHUB_API_URL_DEFAULT = "https://api.github.com";
	public static final String GITHUB_RAW_CONTENT_URL_DEFAULT = "https://raw.githubusercontent.com";
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	// github config variables
	private String GITHUB_API_URL;
	private String GITHUB_RAW_CONTENT_URL;
	protected String owner;
	protected String repo;
	protected String branch;
	
	protected String latest_sha;
	
	public GitHubDB(String owner, String repo, String branch) throws JsonProcessingException, IOException {
		this.initalize(owner, repo, branch, GITHUB_API_URL_DEFAULT, GITHUB_RAW_CONTENT_URL_DEFAULT);
	}
	
	public GitHubDB(String owner, String repo, String branch, String githubApiUrl, String githubResourceUrl) throws JsonProcessingException, IOException {
		this.initalize(owner, repo, branch, githubApiUrl, githubResourceUrl);
	}
	
	
	
	
	
	
	protected void initalize(String owner, String repo, String branch, String githubApiUrl, String githubResourceUrl) {
		this.GITHUB_API_URL = githubApiUrl;
		this.GITHUB_RAW_CONTENT_URL = githubResourceUrl;
		this.owner = owner;
		this.repo = repo;
		this.branch = branch;
		
		this.latest_sha = "";
		
		try {
			URL repoInfoUrl = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/git/refs/heads/" + branch);
			JsonNode repoInfo = getAsJson(repoInfoUrl);
			latest_sha = repoInfo.get("object").get("sha").textValue();
			System.out.println(latest_sha);
		} catch (IOException e) {
			
		}
	}
	
	
	
	
	/* returns true if the latest commit we have does not match the head commit, false otherwise; if we cannot
	 * connect to github, then we just report that new data is not available */
	protected boolean newDataAvailable() {
		URL repoInfoUrl;
		try {
			repoInfoUrl = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/git/refs/heads/" + branch);
			
			JsonNode repoInfo = getAsJson(repoInfoUrl);
			if(!latest_sha.equals(repoInfo.get("object").get("sha").textValue())) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	protected JsonNode methodIndex;
	
	
	protected void refreshMethodIndex() throws JsonProcessingException, IOException {
		//URL methodIndexUrl = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/contents/methods/index.json" + "?ref="+branch);

		URL methodIndexUrl = new URL(GITHUB_RAW_CONTENT_URL + "/" + owner + "/" + repo + "/"+branch+"/methods/index.json");
		
		JsonNode methodIndex = getAsJson(methodIndexUrl);
		System.out.println(methodIndex);
	}
	
	//protected boolean isUpToDate
	
	
	
	public NarrativeMethodData loadMethodData(String methodId) throws JsonProcessingException, IOException {
		
		JsonNode spec     = getResourceAsJson("methods/"+methodId+"/spec.json");
		String description      = getResource("methods/"+methodId+"/description.html");
		String techdescription  = getResource("methods/"+methodId+"/technical_description.html");
		
		NarrativeMethodData data = new NarrativeMethodData(methodId, spec, description, techdescription);
		return data;
	}
	
	
	
	protected JsonNode getResourceAsJson(String path) throws JsonProcessingException, IOException {
		URL url = new URL(GITHUB_RAW_CONTENT_URL + "/" + owner + "/" + repo + "/"+branch+"/"+path);
		return getAsJson(url);
	}
	
	protected String getResource(String path) throws IOException {
		URL url = new URL(GITHUB_RAW_CONTENT_URL + "/" + owner + "/" + repo + "/"+branch+"/"+path);
		return get(url);
	}
	
	protected JsonNode getAsJson(URL url) throws JsonProcessingException, IOException {
		return mapper.readTree(get(url));
	}
	
	protected String get(URL url) throws IOException {
		StringBuilder response = new StringBuilder();
		URLConnection conn = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) 
			response.append(line);
		in.close();
		return response.toString();
	}
	 
	
	
	
	
	
	
	public static void main(String[] args) throws JsonProcessingException, IOException {
		System.out.println("testing github db");
		
		
		GitHubDB githubDB = new GitHubDB("msneddon","narrative_method_specs","master");
		
		NarrativeMethodData data = githubDB.loadMethodData("test_method_1");
		
		System.out.println(data);
		
		return;
	}
	
}