package IntegrationApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;


class Interface
{
	Interface()
		{


		}


	public void run()
		{
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
			catch(Exception e)
			{
			}			
		}
};
