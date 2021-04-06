/*
SimpleWaveManager is a library for reading and writing WAVE audio files.

Copyright 2021 Abir Haque

This file is part of SimpleWaveManager.

SimpleWaveManager is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SimpleWaveManager is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with SimpleWaveManager in the file labeled <LICENSE.txt>.  If not, see <https://www.gnu.org/licenses/>.
*/
 
import java.io.*;
import java.util.*;
/**
 *  <i>SimpleWaveManager</i> 
 *  <br><br>
 *  Library for reading and writing WAVE audio files.
 *  <br><br>
 *  Designed to allow audio input/output code to be portable across different platforms (Windows, Android, Linux, etc.) and Java versions (Java 6 and higher). Targets systems without access to JavaX Sound or Android Media libraries. Does not support audio playback and must use playback features offered by JavaX or Android. Supports mono and stereo file input/output.
 *  <br><br>
 *  @author Abir Haque
 */
public class SimpleWaveManager
{
    /**
     * Header for WAVE files is 44 bytes long.
     */
    public static final int HEADER_LENGTH = 44;
    /**
     * Default sample rate of 44100 hertz.
     */
    public static final int DEFAULT_SAMPLE_RATE = 44100;
    /**
     * Default bits per sample of 16 bits.
     */
    public static final int DEFAULT_BITS_PER_SAMPLE = 16;
    /**
     * Reads bytes within audio file. Header is included.
     * 
     * @param file    File to read.
     * 
     * @return  Byte array.
     */
    public static byte[] getBytes(File file) throws Exception
    {
        int totalBytes = (int)(file.length());
        byte[] bytes = new byte[totalBytes];
        FileInputStream in = new FileInputStream(file);
        in.read(bytes);
        in.close();
        return bytes;
    }
    /**
     * Reads bytes within mono audio file, strips header, and converts remnant bytes into double sound values ranging from -1 to 1.
     * 
     * @param file    File to read.
     * 
     * @return  Double array of sound values. Null if bits per sample is not 8 or 16.
     */
    public static Double[] getMonoValues(File file) throws Exception
    {
        byte[] bytes = getBytes(file);
        if(getBitsPerSample(file)==16)
        {
            int valueLength = (bytes.length-HEADER_LENGTH)/2;
            Double[] values = new Double[valueLength];
            int valueIndex = 0;//Better than (((i-45)-(2*(i-45)))*-1)/2.
            for(int i = HEADER_LENGTH; i < bytes.length; i+=2)
            {
                values[valueIndex] = Double.valueOf(new Byte(bytes[i]))/(Math.pow(2,7));//Convert to 16-bit object.
                valueIndex++;
            }
            return values;
        }
        else if(getBitsPerSample(file)==8)
        {
            int valueLength = (bytes.length-HEADER_LENGTH);
            Double[] values = new Double[valueLength];
            int valueIndex = 0;
            for(int i = HEADER_LENGTH; i < bytes.length; i+=1)
            {
                values[valueIndex] = Double.valueOf(new Byte(bytes[i]))/(Math.pow(2,3));//Convert to 8-bit object.
                valueIndex++;
            }
            return values;
        }
        return null;
    }
    /**
     * Reads bytes within stereo audio file, strips header, and converts remnant bytes into double sound values ranging from -1 to 1. Bytes are split into their respective channels, which is left or right.
     * 
     * @param file    File to read.
     * 
     * @return  ArrayList of two double arrays of sound values, where Double array at index 0 represents the left channel, and double array at index 1 represents the right channel. Null if bits per sample is not 8 or 16.
     */
    public static ArrayList<Double[]> getStereoValues(File file) throws Exception
    {
        byte[] bytes = getBytes(file);
        if(getBitsPerSample(file)==16)
        {
            int valueLength = (bytes.length-HEADER_LENGTH)/2;
            ArrayList<Double[]> values = new ArrayList<Double[]>();
            values.add(new Double[valueLength]);
            values.add(new Double[valueLength]);
            int valueIndex = 0;
            for(int i = HEADER_LENGTH; i < bytes.length; i+=4)
            {
                values.get(0)[valueIndex] = Double.valueOf(new Byte(bytes[i]))/(Math.pow(2,7));
                values.get(1)[valueIndex] = Double.valueOf(new Byte(bytes[i+2]))/(Math.pow(2,7));
                valueIndex++;
            }
            return values;
        }
        else if(getBitsPerSample(file)==8)
        {
            int valueLength = (bytes.length-HEADER_LENGTH);
            ArrayList<Double[]> values = new ArrayList<Double[]>();
            values.add(new Double[valueLength]);
            values.add(new Double[valueLength]);
            int valueIndex = 0;
            for(int i = HEADER_LENGTH; i < bytes.length; i+=2)
            {
                values.get(0)[valueIndex] = Double.valueOf(new Byte(bytes[i]))/(Math.pow(2,3));
                values.get(1)[valueIndex] = Double.valueOf(new Byte(bytes[i+1]))/(Math.pow(2,3));
                valueIndex++;
            }
            return values;
        }
        return null;
    }
    /**
     * Returns integer length of bytes within FMT block of header.
     * 
     * @param file    File to read.
     * 
     * @return  Integer length of bytes within FMT block of header.
     */
    public static Integer getFormatLength(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("FormatLength"));
    }
    /**
     * Returns integer length of bytes within file past the four byte RIFF id.
     * 
     * @param file    File to read.
     * 
     * @return  Integer length of bytes within file past the four byte RIFF id.
     */
    public static Integer getFileLength(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("FileLength"));
    }
    /**
     * Returns PCM value.
     * 
     * @param file    File to read.
     * 
     * @return  Integer PCM value.
     */
    public static Integer getPCMValue(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("PCMValue"));
    }
    /**
     * Returns integer count of channels within file.
     * 
     * @param file    File to read.
     * 
     * @return  Integer count of channels.
     */
    public static Integer getTotalChannels(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("TotalChannels"));
    }
    /**
     * Returns integer sample rate within file in hertz.
     * 
     * @param file    File to read.
     * 
     * @return  Integer sample rate.
     */
    public static Integer getSampleRate(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("SampleRate"));
    }
    /**
     * Returns integer byte rate within file.
     * 
     * @param file    File to read.
     * 
     * @return  Integer byte rate.
     */
    public static Integer getByteRate(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("ByteRate"));
    }
    /**
     * Returns integer bytes per sample within file.
     * 
     * @param file    File to read.
     * 
     * @return  Integer bytes per sample.
     */
    public static Integer getBytesPerSample(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("BytesPerSample"));
    }
    /**
     * Returns integer bits per sample within file.
     * 
     * @param file    File to read.
     * 
     * @return  Integer bits per sample.
     */
    public static Integer getBitsPerSample(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("BitsPerSample"));
    }
    /**
     * Returns integer length of bytes audio values within file past RIFF and FMT header blocks.
     * 
     * @param file    File to read.
     * 
     * @return  Integer length of bytes representing audio values.
     */
    public static Integer getDataLength(File file) throws Exception
    {
        HashMap<String,String> headerContents = getHeaderContents(file);
        return Integer.valueOf(headerContents.get("DataLength"));
    }
    /**
     * Reads header contents of file and places them into Map of String pairs. Helpful source to understanding WAVE format: http://soundfile.sapp.org/doc/WaveFormat/
     * 
     * @param file    File to read.
     * 
     * @return  Map of header content pairs.
     */
    private static HashMap<String,String> getHeaderContents(File file) throws Exception
    {
        byte[] bytes = getBytes(file);
        HashMap<String,String> headerContents = new HashMap<>();
        headerContents.put("FileID",String.valueOf((char) (bytes[0] & 0xFF))+String.valueOf((char) (bytes[1] & 0xFF))+String.valueOf((char) (bytes[2] & 0xFF))+String.valueOf((char) (bytes[3] & 0xFF)));//Convert byte to hex to char to String.
        headerContents.put("FileLength",String.valueOf(bytes[4]+bytes[5]+bytes[6] +bytes[7]));//4 + (8 + FormatLength) + (8 + DataLength)
        headerContents.put("FileType",String.valueOf((char) (bytes[8] & 0xFF))+String.valueOf((char) (bytes[9] & 0xFF))+String.valueOf((char) (bytes[10] & 0xFF))+String.valueOf((char) (bytes[11] & 0xFF)));
        headerContents.put("FormatMarker",String.valueOf((char) (bytes[12] & 0xFF))+String.valueOf((char) (bytes[13] & 0xFF))+String.valueOf((char) (bytes[14] & 0xFF))+String.valueOf((char) (bytes[15] & 0xFF)));
        headerContents.put("FormatLength",String.valueOf(bytes[16]+bytes[17]+bytes[18]+bytes[19]));
        headerContents.put("PCMValue",String.valueOf(bytes[20]+bytes[21]));
        headerContents.put("TotalChannels",String.valueOf(bytes[22]+bytes[23]));
        headerContents.put("SampleRate",String.valueOf((bytes[24]<<0)& 0xFF|(bytes[25]<<8)& 0xFFFF|(bytes[26]<<16)& 0xFFFFFF|(bytes[27]<<24)& 0xFFFFFFFF));//32 bit integer
        headerContents.put("ByteRate",String.valueOf((bytes[28]<<0)& 0xFF|(bytes[29]<<8)& 0xFFFF|(bytes[30]<<16)& 0xFFFFFF|(bytes[31]<<24)& 0xFFFFFFFF));//SampleRate * NumChannels * BitsPerSample/8
        headerContents.put("BytesPerSample",String.valueOf(bytes[32]+bytes[33]));//NumChannels * BitsPerSample/8
        headerContents.put("BitsPerSample",String.valueOf(bytes[34]+bytes[35]));
        headerContents.put("DataMarker",String.valueOf((char) (bytes[36] & 0xFF))+String.valueOf((char) (bytes[37] & 0xFF))+String.valueOf((char) (bytes[38] & 0xFF))+String.valueOf((char) (bytes[39] & 0xFF)));
        headerContents.put("DataLength",String.valueOf(bytes[40]+bytes[41]+bytes[42]+bytes[43]));//NumSamples * NumChannels * BitsPerSample/8

        return headerContents;
    }
    /**
     * Writes bytes into file.
     * 
     * @param bytes    Bytes to write.
     * @param fileName    Name of output file.
     */
    public static void write(byte[] bytes, String fileName) throws Exception
    {
        File file = new File(fileName);
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }
    /**
     * Writes bytes into mono audio file with default sample rate of 44100 hertz and bits per sample of 16 bits.
     * 
     * @param values    Double values to convert, then write.
     * @param fileName    Name of output file.
     */
    public static void write(Double[] values, String fileName) throws Exception
    {
        write(values, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE, fileName);
    }
    /**
     * Writes bytes into mono audio file with custom sample rate and bits per sample.
     * 
     * @param values    Double values to convert, then write.
     * @param sampleRate    Sample rate.
     * @param bitsPerSample    Bits per sample. Can only output 8-bit or 16-bit audio.
     * @param fileName    Name of output file.
     */
    public static void write(Double[] values, int sampleRate, int bitsPerSample, String fileName) throws Exception
    {
        int byteLength = (values.length * bitsPerSample/8)+HEADER_LENGTH;
        byte[] bytes = new byte[byteLength];
        bytes[0] = 'R';
        bytes[1] = 'I';
        bytes[2] = 'F';
        bytes[3] = 'F';
        bytes[4] = (byte) ((36 + values.length) & 0xFF);
        bytes[5] = (byte) (((36 + values.length) >> 8) & 0xFFFF);
        bytes[6] = (byte) (((36 + values.length) >> 16) & 0xFFFFFF);
        bytes[7] = (byte) (((36 + values.length) >> 24) & 0xFFFFFFFF);
        bytes[8] = 'W';
        bytes[9] = 'A';
        bytes[10] = 'V';
        bytes[11] = 'E';
        bytes[12] = 'f';
        bytes[13] = 'm';
        bytes[14] = 't';
        bytes[15] = ' ';
        bytes[16] = 16;
        bytes[17] = 0;
        bytes[18] = 0;
        bytes[19] = 0;
        bytes[20] = 1;
        bytes[21] = 0;
        bytes[22] = (byte) 1;
        bytes[23] = 0;
        bytes[24] = (byte) (sampleRate & 0xFF);
        bytes[25] = (byte) ((sampleRate >> 8) & 0xFFFF);
        bytes[26] = (byte) ((sampleRate >> 16) & 0xFFFFFF);
        bytes[27] = (byte) ((sampleRate >> 24) & 0xFFFFFFFF);
        bytes[28] = (byte) ((sampleRate*bitsPerSample/8) & 0xFF);
        bytes[29] = (byte) (((sampleRate*bitsPerSample/8) >> 8) & 0xFFFF);
        bytes[30] = (byte) (((sampleRate*bitsPerSample/8) >> 16) & 0xFFFFFF);
        bytes[31] = (byte) (((sampleRate*bitsPerSample/8) >> 24) & 0xFFFFFFFF);
        bytes[32] = (byte) (bitsPerSample/8);
        bytes[33] = 0;
        bytes[34] = (byte) bitsPerSample;
        bytes[35] = 0;
        bytes[36] = 'd';
        bytes[37] = 'a';
        bytes[38] = 't';
        bytes[39] = 'a';
        bytes[40] = (byte) ((values.length * bitsPerSample/8)  & 0xFF);
        bytes[41] = (byte) (((values.length * bitsPerSample/8) >> 8) & 0xFFFF);
        bytes[42] = (byte) (((values.length * bitsPerSample/8) >> 16) & 0xFFFFFF);
        bytes[43] = (byte) (((values.length * bitsPerSample/8) >> 24) & 0xFFFFFFFF);
        int byteIndex = HEADER_LENGTH;
        for(int i = 0; i < values.length; i++)
        {
            if(bitsPerSample==16)
            {
                bytes[byteIndex] =(byte)(values[i]*Math.pow(2,bitsPerSample/2-1));
                bytes[byteIndex+1] =(byte)(values[i]*Math.pow(2,bitsPerSample/2-1));
                byteIndex+=2;
            }
            if(bitsPerSample==8)
            {
                bytes[byteIndex] = (byte)(values[i]*Math.pow(2,bitsPerSample/2-1));
                byteIndex+=1;
            }
        }
        write(bytes, fileName);
    }
    /**
     * Writes bytes into stereo audio file with default sample rate of 44100 hertz and bits per sample of 16 bits.
     * 
     * @param channel1Values    Double values to convert, then write into left channel.
     * @param channel2Values    Double values to convert, then write into right channel.
     * @param fileName    Name of output file.
     */
    public static void write(Double[] channel1Values, Double[] channel2Values, String fileName) throws Exception
    {
        write(channel1Values, channel2Values, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE, fileName);
    }
    /**
     * Writes bytes into stereo audio file with custom sample rate and bits per sample.
     * 
     * @param channel1Values    Double values to convert, then write into left channel.
     * @param channel2Values    Double values to convert, then write into right channel.
     * @param sampleRate    Sample rate.
     * @param bitsPerSample    Bits per sample. Can only output 8-bit or 16-bit audio.
     * @param fileName    Name of output file.
     */
    public static void write(Double[] channel1Values, Double[] channel2Values, int sampleRate, int bitsPerSample, String fileName) throws Exception
    {
        int byteLength = (channel1Values.length*2*bitsPerSample/8)+HEADER_LENGTH;
        byte[] bytes = new byte[byteLength];
        bytes[0] = 'R';
        bytes[1] = 'I';
        bytes[2] = 'F';
        bytes[3] = 'F';
        bytes[4] = (byte) ((36 + (channel1Values.length * 2 * bitsPerSample/8)) & 0xFF);
        bytes[5] = (byte) (((36 + (channel1Values.length * 2 * bitsPerSample/8)) >> 8) & 0xFFFF);
        bytes[6] = (byte) (((36 + (channel1Values.length * 2 * bitsPerSample/8)) >> 16) & 0xFFFFFF);
        bytes[7] = (byte) (((36 + (channel1Values.length * 2 * bitsPerSample/8)) >> 24) & 0xFFFFFFFF);
        bytes[8] = 'W';
        bytes[9] = 'A';
        bytes[10] = 'V';
        bytes[11] = 'E';
        bytes[12] = 'f';
        bytes[13] = 'm';
        bytes[14] = 't';
        bytes[15] = ' ';
        bytes[16] = 16;
        bytes[17] = 0;
        bytes[18] = 0;
        bytes[19] = 0;
        bytes[20] = 1;
        bytes[21] = 0;
        bytes[22] = (byte) 2;
        bytes[23] = 0;
        bytes[24] = (byte) (sampleRate & 0xFF);
        bytes[25] = (byte) ((sampleRate >> 8) & 0xFFFF);
        bytes[26] = (byte) ((sampleRate >> 16) & 0xFFFFFF);
        bytes[27] = (byte) ((sampleRate >> 24) & 0xFFFFFF);
        bytes[28] = (byte) ((sampleRate*2*bitsPerSample/8) & 0xFF);
        bytes[29] = (byte) (((sampleRate*2*bitsPerSample/8) >> 8) & 0xFFFF);
        bytes[30] = (byte) (((sampleRate*2*bitsPerSample/8) >> 16) & 0xFFFFFF);
        bytes[31] = (byte) (((sampleRate*2*bitsPerSample/8) >> 24) & 0xFFFFFFFF);
        bytes[32] = (byte) (2 * bitsPerSample/8);
        bytes[33] = 0;
        bytes[34] = (byte) bitsPerSample;
        bytes[35] = 0;
        bytes[36] = 'd';
        bytes[37] = 'a';
        bytes[38] = 't';
        bytes[39] = 'a';
        bytes[40] = (byte) (channel1Values.length * 2 * bitsPerSample/8 & 0xFF);
        bytes[41] = (byte) ((channel1Values.length * 2 * bitsPerSample/8 >> 8) & 0xFFFF);
        bytes[42] = (byte) ((channel1Values.length * 2 * bitsPerSample/8 >> 16) & 0xFFFFFF);
        bytes[43] = (byte) ((channel1Values.length * 2 * bitsPerSample/8 >> 24) & 0xFFFFFFFF);
        int byteIndex = HEADER_LENGTH;
        for(int i = 0; i < channel1Values.length; i++)
        {
            if(bitsPerSample==16)
            {
                bytes[byteIndex] = (byte)(channel1Values[i]*Math.pow(2,bitsPerSample/2-1));
                bytes[byteIndex+1] =(byte)(channel1Values[i]*Math.pow(2,bitsPerSample/2-1));
                bytes[byteIndex+2] =(byte)(channel2Values[i]*Math.pow(2,bitsPerSample/2-1));
                bytes[byteIndex+3] =(byte)(channel2Values[i]*Math.pow(2,bitsPerSample/2-1));
                byteIndex+=4;
            }
            if(bitsPerSample==8)
            {
                bytes[byteIndex] = (byte)(channel1Values[i]*Math.pow(2,bitsPerSample/2-1));
                bytes[byteIndex+1] =(byte)(channel2Values[i]*Math.pow(2,bitsPerSample/2-1));
                byteIndex+=2;
            }
        }
        write(bytes, fileName);
    }
    /**
     * Unit test writing and printing mono audio file values, and writing two identical mono audio files using different write methods and two identical stereo audio files using different write methods.
     */
    public static void main() throws Exception
    {
        Double[] values1 = new Double[44100];
        Double[] values2 = new Double[44100];
        for(int i = 0; i < values1.length; i++)
        {
            values1[i] = 0.5 * Math.sin(2*Math.PI * 440 * i / 44100);
        }
        for(int i = 0; i < values2.length; i++)
        {
            values2[i] = 0.5 * Math.sin(2*Math.PI * 500 * i / 44100);
        }
        write(values1, "1.wav");
        write(values2, 44100, 16, "2.wav");
        write(values1, values2,"3.wav");
        write(values1, values2, 44100, 16, "4.wav");
        Double[] values3 = new Double[4];
        for(int i = 0; i < values3.length; i++)
        {
            values3[i] = 0.5 * Math.sin(2*Math.PI * 440 * i / 44100);
        }
        write(values3, "3.wav");
        Double[] readValues = getMonoValues(new File("3.wav"));
        for (int i =0; i < readValues.length;i++)
        {
            System.out.println("i = " + i + " >>" + readValues[i]);
        }
    }
}