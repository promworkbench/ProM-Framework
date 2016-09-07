package org.processmining.framework.util;

import jargs.gnu.CmdLineParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AutoHelpCommandLineParser extends CmdLineParser {
	private final List<Pair<Option, String>> optionHelpStrings = new ArrayList<Pair<Option, String>>();
	private final String programName;
	private final Command[] commands;

	public static abstract class Command {
		private final String name;
		private final String help;

		public Command(String name, String help) {
			this.name = name;
			this.help = help;
		}

		public abstract int run(List<String> args) throws Exception;

		public String getName() {
			return name;
		}

		public String getHelp() {
			return help;
		}
	}

	public AutoHelpCommandLineParser(String programName) {
		this(programName, null);
	}

	public AutoHelpCommandLineParser(String programName, Command[] commands) {
		this.programName = programName;
		this.commands = commands;
	}

	public Option addHelp(Option option, String helpString) {
		optionHelpStrings.add(new Pair<Option, String>(option, helpString));
		return option;
	}

	public void printUsage() {
		if (commands != null) {
			System.err.println("Usage: " + programName + " [options] COMMAND [command arguments]");
		} else {
			System.err.println("Usage: " + programName + " [options]");
		}

		int width = 0;
		for (Pair<Option, String> option : optionHelpStrings) {
			width = Math.max(width, getOptionString(option).length());
		}
		width = Math.min(25, width);

		for (Pair<Option, String> option : optionHelpStrings) {
			System.err.println(String.format("  %-" + width + "s  %s", getOptionString(option), option.getSecond()));
		}

		if (commands != null) {
			width = 0;

			for (Command c : commands) {
				width = Math.max(width, c.getName().length());
			}
			width = Math.min(25, width);

			System.err.println("The following commands are available:");
			for (Command c : commands) {
				System.err.println(String.format("  %-" + width + "s  %s", c.getName(), c.getHelp()));
			}
		}
	}

	private String getOptionString(Pair<Option, String> option) {
		return "-" + option.getFirst().shortForm() + " / --" + option.getFirst().longForm();
	}

	public int runCommand() throws Exception {
		LinkedList<String> arguments = new LinkedList<String>(Arrays.asList(getRemainingArgs()));
		String commandName = arguments.removeFirst();

		for (Command c : commands) {
			if (c.getName().equals(commandName)) {
				return c.run(arguments);
			}
		}
		return -1;
	}
}
