package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import app.service.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

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

}