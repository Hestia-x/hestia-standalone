package huck.hestia.db.memory;

import huck.hestia.db.memory.HestiaMemoryDB.Dumper;
import huck.hestia.db.memory.HestiaMemoryDB.Loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileDataManager {
	public static Loader getLoader(File file) throws FileNotFoundException, Exception {
		return TextDataManager.getLoader(file.getName(), new FileInputStream(file));
	}

	public static Dumper getDumper(File file) throws FileNotFoundException, Exception {
		return TextDataManager.getDumper(new FileOutputStream(file));
	}
}
