package org.oisems.node;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Main {

	public static void main(String args[]) {
		System.out.println("oisems node v.1.0 ");
		Node node = new Node();
		System.out.println("node ID " + node.getNodeId());
		// create Options object
		Options options = new Options();

		int receive_port = 44444;
		int broadcast_port = 44445;

		// add t option
		options.addOption("rport", true,
				"define the receive port default (44444)");
		options.addOption("bport", true,
				"define the UDP broadcast port (44445)");
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("rport")) {
				receive_port = Integer.parseInt(cmd
						.getOptionValue("rport"));
			}
			if (cmd.hasOption("bport")) {
				broadcast_port = Integer.parseInt(cmd
						.getOptionValue("bport"));
			}
			node.start(receive_port, broadcast_port);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
