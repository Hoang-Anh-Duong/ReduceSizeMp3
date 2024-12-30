package org.example;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import net.sourceforge.lame.mp3.MPEGMode;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Main {
    public static void compressMP3(File inputFile, int bitrate) throws IOException, UnsupportedAudioFileException {
        File outputFile = new File("file-output/" + inputFile.getName());

        MpegAudioFileReader mpegReader = new MpegAudioFileReader();
        AudioInputStream audioInputStream = mpegReader.getAudioInputStream(inputFile);
        AudioFormat baseFormat = audioInputStream.getFormat();

        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
        );

        try (AudioInputStream decodedAudioInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream);
             OutputStream outputStream = new FileOutputStream(outputFile)) {

            LameEncoder encoder = new LameEncoder(decodedFormat, bitrate, MPEGMode.STEREO, Lame.QUALITY_HIGH, false);
            byte[] buffer = new byte[encoder.getPCMBufferSize()];
            byte[] encodedBuffer = new byte[encoder.getMP3BufferSize()];
            int bytesRead;

            while ((bytesRead = decodedAudioInputStream.read(buffer)) > 0) {
                int bytesEncoded = encoder.encodeBuffer(buffer, 0, bytesRead, encodedBuffer);
                outputStream.write(encodedBuffer, 0, bytesEncoded);
            }

            int bytesFlushed = encoder.encodeFinish(encodedBuffer);
            outputStream.write(encodedBuffer, 0, bytesFlushed);
        }
        System.out.println("Compressed MP3 file is saved at " + outputFile.getName());
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        int bitrate = 64; // desired bitrate in kbps
        File directory = new File("file-input/");

        if (!directory.isDirectory()) {
            System.out.println("Provided path is not a directory.");
            return;
        }

        File[] filesList = directory.listFiles();
        int index = 0;
        if (filesList != null) {
            for (File file : filesList) {
                if (file.isDirectory()) {
                    System.out.println("Directory: " + file.getName());
                } else {
                    System.out.println("File: " + file.getName());
                    compressMP3(file, bitrate);
                    index++;
                    System.out.println("Progress: " + Math.round(index * 100 /filesList.length));
                }
            }
        } else {
            System.out.println("Directory is empty or an error occurred.");
        }
    }
}
