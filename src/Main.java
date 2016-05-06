

class IntegrationApp
{
	public static void main()
		{
			String[] cmd = new String[]{"/bin/sh", "bash/vm.sh"};
			Process pr = Runtime.getRuntime().exec(cmd);
		}

};
