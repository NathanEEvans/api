package com.stormcloud.ide.api.github;

import java.io.IOException;
import java.util.List;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author martijn
 */
public class GitHubManager {

    public static void main(String[] args) {


        new GitHubManager().run();


    }

    public void run() {


        GitHubClient client = new GitHubClient();
        client.setCredentials("martijn-teigeler", "B3nj@m1nRocks");


        try {

            RepositoryService service = new RepositoryService(client);

            for (Repository repo : service.getRepositories()) {

                System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
            }


            IssueService issueService = new IssueService(client);

            List<RepositoryIssue> issues = issueService.getIssues();

            for (RepositoryIssue issue : issues) {

                System.out.println("#" + issue.getNumber() + " " + issue.getTitle());
            }



        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
