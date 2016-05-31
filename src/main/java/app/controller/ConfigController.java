package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import app.service.Config;

import java.io.*;
import java.util.*;

@Controller
public class ConfigController {

    @RequestMapping(value="/config", method=RequestMethod.GET)
    public String greetingForm(Model model) {
        model.addAttribute("config", new Config());
        return "config";
    }

    @RequestMapping(value="/config", method=RequestMethod.POST)
    public String greetingSubmit(@ModelAttribute Config config, Model model) {
        //grab values from the config model and save to text file

        //grab values from config object
        String userName = config.getId();
        String[] repoAry = config.getRepos();

        //append the information to file IntegrationApp/resources/listOfRepositories.txt
        try {
            File file = new File(System.getProperty("user.dir") + "/resources/listOfRepositories.txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, true);
            PrintWriter out = new PrintWriter(fw);

            String inputStr;
            for(int i = 0; i<repoAry.length; i++) {
                inputStr = userName + '/' + repoAry[i] + '\n';
                out.write(inputStr);
            }
            out.close();
        } catch (Exception e) {
            System.out.println("Error writing request:" + e);
        }

        model.addAttribute("config", config);
        return "result";
    }

    @RequestMapping(value="/repo", method= RequestMethod.GET)
    public String showRepos(@ModelAttribute Config config, Model model) {

        //grab all the users from directory IntegrationApp/output/{username}/{repo}/{pullnumber}
        File file = new File(System.getProperty("user.dir") + "/output");
        Map< String, Map< String, String[] > > userMap = new HashMap<>();

        String[] userNames = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        System.out.print(Arrays.toString(userNames));

        //iterate through each user name and grab repo directory names
        for (String userName: userNames) {
            File userDir = new File(System.getProperty("user.dir") + "/output/" + userName);
            String[] repoNames = userDir.list(new FilenameFilter(){
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });
            System.out.print('\t' + Arrays.toString(repoNames));
            //make temp map to hold the repo info
            Map<String, String[]> temp = new HashMap<>();
            for (String repoName: repoNames) {
                File repoDir = new File(System.getProperty("user.dir") + "/output/" + userName + '/' + repoName);
                String[] outputs = repoDir.list(new FilenameFilter(){
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isFile();
                    }
                });
                System.out.print("\t\t" + Arrays.toString(outputs));
                //add output file names to map
                temp.put(repoName, outputs);
            }
            //after all repos have been updated with output logs, add that map to user map
            userMap.put(userName, temp);
        }
        
        model.addAttribute("userMap", userMap);
        return "repos";
    }

}