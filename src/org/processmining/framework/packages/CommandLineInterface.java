package org.processmining.framework.packages;

import jargs.gnu.CmdLineParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.util.AutoHelpCommandLineParser;
import org.processmining.framework.util.Pair;

public class CommandLineInterface {

	private final PackageManager manager;

	public CommandLineInterface(PackageManager manager) {
		this.manager = manager;
	}

	public int run(String[] args) {
		AutoHelpCommandLineParser.Command[] commands = { new UpdateCommand(manager),
				new InstallOrRemoveCommand(manager), new ListCommand(manager) };
		AutoHelpCommandLineParser parser = new AutoHelpCommandLineParser("java "
				+ PackageManager.class.getCanonicalName(), commands);

		CmdLineParser.Option helpOption = parser.addHelp(parser.addBooleanOption('h', "help"),
				"Print this help message and exit");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			parser.printUsage();
			return 1;
		}

		if ((parser.getRemainingArgs().length == 0) || (Boolean) parser.getOptionValue(helpOption, Boolean.FALSE)) {
			parser.printUsage();
			return 0;
		}

		int exitcode;
		try {
			manager.initialize(Level.ALL);
			exitcode = parser.runCommand();
		} catch (Throwable e) {
			e.printStackTrace();
			return 1;
		}

		if (exitcode == -1) {
			parser.printUsage();
			return 1;
		}
		return exitcode;
	}

}

class UpdateCommand extends AutoHelpCommandLineParser.Command {

	private final PackageManager manager;

	public UpdateCommand(PackageManager manager) {
		super("update", "Retrieve the latest package definitions from all repositories");
		this.manager = manager;
	}

	@Override
	public int run(List<String> args) throws Exception {
		System.out.println("Updating...");
		manager.update(true, Level.ALL);
		System.out.println("Done.");
		return 0;
	}

}

class InstallOrRemoveCommand extends AutoHelpCommandLineParser.Command {

	private final PackageManager manager;

	public InstallOrRemoveCommand(PackageManager manager) {
		super("change",
				"Install the packages preceded by +, remove packages preceded by x (example: change +packageA:1.0 xpackageB)");
		this.manager = manager;
	}

	@Override
	public int run(List<String> args) throws Exception {
		List<PackageDescriptor> toInstall = new ArrayList<PackageDescriptor>();
		List<PackageDescriptor> toRemove = new ArrayList<PackageDescriptor>();

		Set<PackageDescriptor> all = new HashSet<PackageDescriptor>(manager.getInstalledPackages());
		all.addAll(manager.getAvailablePackages());
		Map<String, SortedSet<PackageDescriptor>> map = PackageManager.getPackageMap(all);

		for (String packageNameAndAction : args) {
			if ((packageNameAndAction.length() > 1)
					&& ((packageNameAndAction.charAt(0) == 'x') || (packageNameAndAction.charAt(0) == '+'))) {
				Pair<String, PackageVersion> packageName = parse(packageNameAndAction.substring(1));
				boolean install = packageNameAndAction.charAt(0) == '+';
				Set<PackageDescriptor> versions = map.get(packageName.getFirst());
				PackageDescriptor p = null;

				if ((versions != null) && !versions.isEmpty()) {
					if (packageName.getSecond() == null) {
						// take latest version if no version was given
						p = versions.toArray(new PackageDescriptor[0])[versions.size() - 1];
					} else {
						for (PackageDescriptor v : versions) {
							if (v.getVersion().equals(packageName.getSecond())) {
								p = v;
								break;
							}
						}
					}
				}

				if (p == null) {
					System.out.println("Could not find package " + packageNameAndAction.substring(1)
							+ " in the list of available packages, skipping.");
				} else {
					System.out.println("Selected " + p + " for " + (install ? "installation" : "removal") + "...");
					if (install) {
						toInstall.add(p);
					} else {
						toRemove.add(p);
					}
				}
			} else {
				System.out
						.println("Invalid package specification (please use + or - to indicate installation or removal), skipping.");
			}
		}

		if (toInstall.isEmpty() && toRemove.isEmpty()) {
			System.out.println("Nothing to install or remove.");
		} else {
			System.out.println("Starting installation...");
			manager.install(toInstall);
			manager.uninstall(toRemove);
			PackageStateReport report = manager.getLatestReport();

			System.out.print(report);
			System.out.println(report.hasErrors() ? "Installation is NOT performed." : "Installation done.");
		}
		return 0;
	}

	private Pair<String, PackageVersion> parse(String name) {
		int hyphen = name.indexOf(':');

		if (hyphen >= 0) {
			return new Pair<String, PackageVersion>(name.substring(0, hyphen), new PackageVersion(
					name.substring(hyphen + 1)));
		} else {
			return new Pair<String, PackageVersion>(name, null);
		}
	}
}

class ListCommand extends AutoHelpCommandLineParser.Command {

	private final PackageManager manager;

	public ListCommand(PackageManager manager) {
		super("list", "List all known packages and their status (A=available,I=installed,B=broken,+=has update)");
		this.manager = manager;
	}

	@Override
	public int run(List<String> args) throws Exception {
		Set<PackageDescriptor> installed = manager.getInstalledPackages();
		Collection<PackageDescriptor> enabled = manager.getEnabledPackages();

		Set<PackageDescriptor> all = new HashSet<PackageDescriptor>(manager.getInstalledPackages());
		all.addAll(manager.getAvailablePackages());

		for (Map.Entry<String, SortedSet<PackageDescriptor>> item : PackageManager.getPackageMap(all).entrySet()) {
			PackageDescriptor installedPackage = null;
			PackageVersion highestVersion = null;
			boolean isEnabled = false;
			String versions = "";

			for (PackageDescriptor pack : item.getValue()) {
				if (installed.contains(pack)) {
					installedPackage = pack;
				}
				if (enabled.contains(pack)) {
					isEnabled = true;
				}
				if (versions.length() > 0) {
					versions += ", ";
				}
				versions += pack.getVersion();
				highestVersion = pack.getVersion();
			}

			if (installedPackage == null) {
				System.out.println("A  " + item.getKey() + " [" + versions + "]");
			} else if (isEnabled) {
				System.out.println("I" + (installedPackage.getVersion().lessThan(highestVersion) ? "+" : " ") + " "
						+ installedPackage + " [" + versions + "]");
			} else {
				System.out.println("B" + (installedPackage.getVersion().lessThan(highestVersion) ? "+" : " ") + " "
						+ installedPackage + " [" + versions + "]");
			}
		}
		return 0;
	}
}
