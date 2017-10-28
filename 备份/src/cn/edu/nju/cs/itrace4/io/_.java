package cn.edu.nju.cs.itrace4.io;

import cn.edu.nju.cs.itrace4.core.metrics.Result;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by niejia on 15/2/10.
 */
public class _ {

    public static void abort(String m) {
        System.err.println(m);
        System.err.flush();
        Thread.dumpStack();
        System.exit(1);
    }

    @Nullable
    public static String readFile(@NotNull String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return Charset.forName("UTF-8").decode(ByteBuffer.wrap(encoded)).toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void writeFile(@NotNull String input, String path) {
        Path outPath = Paths.get(path);
        Charset charset = Charset.forName("UTF-8");

        try (BufferedWriter writer = Files.newBufferedWriter(outPath, charset)) {
            writer.write(input, 0, input.length());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    public static  <K, V extends Comparable<? super V>> Map<K, V>
    sortValueByDescending( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>( map.entrySet() );
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public static  <K, V extends Comparable<? super V>> Map<K, V>
    sortValueByAscending( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>( map.entrySet() );
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public static void compare(Result ours, Result compareTo) {
        List<Double> oursPrecisionList = ours.getPrecisionAtRecallByTen();
        List<Double> compareToPrecisionList = compareTo.getPrecisionAtRecallByTen();
        List<Integer> oursFP = ours.getFalsePositiveAtRecallByTen();
        List<Integer> compareFP = compareTo.getFalsePositiveAtRecallByTen();

        int recall = 10;

        for (int i = 0; i < oursPrecisionList.size(); i++) {
            double ourPrecision = oursPrecisionList.get(i);
            double theirPrecision = compareToPrecisionList.get(i);
            System.out.println("Recall " + recall);
            System.out.println(ourPrecision - theirPrecision);
            int ourFP = oursFP.get(i);
            int theirFP = compareFP.get(i);
            System.out.println(ourFP - theirFP);
            recall += 10;
        }
    }
}
