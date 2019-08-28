package com.uitgis.plugin.tilegenerator.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassesInJarsTest {

	private static byte[] copyStream(InputStream in, JarEntry entry) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		long size = entry.getSize();
		if (size > -1) {
			byte[] buffer = new byte[1024 * 4];
			int n = 0;
			long count = 0;
			while (-1 != (n = in.read(buffer)) && count < size) {
				baos.write(buffer, 0, n);
				count += n;
			}
		} else {
			while (true) {
				int b = in.read();
				if (b == -1) {
					break;
				}
				baos.write(b);
			}
		}
		baos.close();
		return baos.toByteArray();
	}

	public static void extractClassesFromJar(InputStream fileJarName) throws IOException {

		JarInputStream inputJar = new JarInputStream(fileJarName);

		try {
			JarEntry entryJar;
			while ((entryJar = inputJar.getNextJarEntry()) != null) {

				if ((entryJar.getName().endsWith(".jar"))) {
					System.out.println("Jar File Name----->" + entryJar.getName());
					byte[] byteArray = copyStream(inputJar, entryJar);
					extractClassesFromJar(new ByteArrayInputStream(byteArray));
				} else if ((entryJar.getName().endsWith(".class"))) {
					String className = entryJar.getName().replaceAll("/", "\\.");
					String myClass = className.substring(0, className.lastIndexOf('.'));
					System.out.println("Class is: " + myClass);
				}

			}

		} catch (Exception e) {
			System.out.println("Oops.. Encounter an issue while parsing jar" + e.toString());
		} finally {
			inputJar.close();
		}
		return;
	}

	public static void main(String[] args) throws IOException {

		try {
			extractClassesFromJar(
					new FileInputStream("C:\\ProjDevs\\GeoDevJD\\Maple 1.1\\plugin\\TileGenerator\\TileGenerator.jar"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}