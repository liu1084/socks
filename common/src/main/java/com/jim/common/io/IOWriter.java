package com.jim.common.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface IOWriter {

	int write(ByteBuffer buffer) throws IOException;

}
