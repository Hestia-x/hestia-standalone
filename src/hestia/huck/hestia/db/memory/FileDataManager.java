package huck.hestia.db.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileDataManager extends BinaryDataManager {
	private File file;
	public FileDataManager(File file) {
		this.file = file;
	}
	@Override
	protected ReadableByteChannel getReadableByteChannel() throws FileNotFoundException {
		return Channels.newChannel(new FileInputStream(file));
	}

	@Override
	protected WritableByteChannel getWritableByteChannel() throws FileNotFoundException {
		return Channels.newChannel(new FileOutputStream(file));
	}
}
