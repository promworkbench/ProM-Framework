package org.processmining.framework.util;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.ImageIcon;

import org.processmining.framework.boot.Boot;

public class IconLoader {

	public static ImageIcon getStandardIcon(String name) {
		String imgLocation = "toolbarButtonGraphics/" + name + ".gif";
		java.net.URL imageURL = Thread.currentThread().getContextClassLoader().getResource(imgLocation);

		return new ImageIcon(imageURL);
	}

	public static ImageIcon getProMLibIcon(String name) {
		try {
			File file = new File(Boot.IMAGES_FOLDER + File.separator + name + ".gif");
			java.net.URL imageURL = file.toURI().toURL();
			return new ImageIcon(imageURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
