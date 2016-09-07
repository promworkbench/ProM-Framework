package org.processmining.framework.packages;

import java.net.URL;

public class Repository {

	private final URL url;

	public Repository(URL url) {
		this.url = url;
	}

	public URL getURL() {
		return url;
	}

	public String toString() {
		return "Repository(" + getURL().toString() + ")";
	}

	public boolean equals(Object other) {
		if (!(other instanceof Repository)) {
			return false;
		}
		return ((Repository) other).getURL().equals(url);
	}

	public int hashCode() {
		return url.hashCode();
	}
}
