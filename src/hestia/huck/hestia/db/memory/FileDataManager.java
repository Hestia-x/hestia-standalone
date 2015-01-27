package huck.hestia.db.memory;

import huck.hestia.db.memory.HestiaMemoryDB.Dumper;
import huck.hestia.db.memory.HestiaMemoryDB.Loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.Channels;

public class FileDataManager {
	public static Loader getLoader(File file) throws FileNotFoundException, Exception {
		return BinaryDataManager.getLoader(file.getName(), Channels.newChannel(new FileInputStream(file)));
	}

	public static Dumper getDumper(File file) throws FileNotFoundException, Exception {
		return BinaryDataManager.getDumper(Channels.newChannel(new FileOutputStream(file)));
	}
}
