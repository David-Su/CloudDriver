package cloud.bean;

import java.util.List;

public class CloudFile {
	public String name;
	public boolean isDir;
	public List<CloudFile> children;
}
