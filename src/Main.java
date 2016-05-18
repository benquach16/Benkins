package IntegrationApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

class IntegrationApp
{
	public static void main(String[] args)
		{
			Interface d;
			d = new Interface();
			try{
				String[] cmd = new String[]{"/bin/sh", "/home/ben/IntegrationApp/src/bash/vm.sh"};

				Process pr = Runtime.getRuntime().exec(cmd);
				pr.waitFor();
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));

				String line = "";

				while ((line=buf.readLine())!=null) {

					System.out.println(line);

				}
			System.out.print("sdf");
			}
			catch(Exception e)
			{
			}			
		}

};
