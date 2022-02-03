package drsit.utils.reloader;

import java.util.Collection;

public interface Reloadable {

	public void reload();
	
	public Collection<String> getReloadFilenames();
	
}
