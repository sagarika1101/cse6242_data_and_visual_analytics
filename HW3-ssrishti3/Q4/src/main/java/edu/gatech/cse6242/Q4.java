package edu.gatech.cse6242;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;

public class Q4 {

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{
        private final static IntWritable wtOut = new IntWritable(1);
        private final static IntWritable wtIn = new IntWritable(-1); //for diff, add out degree and subtract in degree
        private Text graphNode = new Text();
        
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
            String line = value.toString();
            String nodes[] = line.split("\t");

            graphNode.set(nodes[0]);
            context.write(graphNode,wtOut);  //source entry counts in out degree

            graphNode.set(nodes[1]);
            context.write(graphNode,wtIn);   //target entry counts in in degree
        }
    }
    
    public static class TokenizerMapper2 extends Mapper<Object, Text, Text, IntWritable>{
        private final static IntWritable diff = new IntWritable(1);
        private final static Text count = new Text();
        
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
            String line = value.toString();
            String nodes[] = line.split("\\t");

            count.set(nodes[1]);  //we want count of each 'diff in out and in degree'
            context.write(count,diff);
        }
    }

    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
        private IntWritable result = new IntWritable();
        
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException{
            int sum = 0;
            for(IntWritable val : values){
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job jobA = Job.getInstance(conf, "Q4a");
    String OUTPUT_PATH = "partOutput";

    /* TODO: Needs to be implemented */
    //determining in/out values for source and target
    jobA.setJarByClass(Q4.class);
    jobA.setMapperClass(TokenizerMapper.class);
    jobA.setCombinerClass(IntSumReducer.class);
    jobA.setReducerClass(IntSumReducer.class);
    jobA.setOutputKeyClass(Text.class);
    jobA.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(jobA, new Path(args[0]));
    FileOutputFormat.setOutputPath(jobA, new Path(OUTPUT_PATH));
    jobA.waitForCompletion(true);

    //word count over previous output
    Job jobB = Job.getInstance(conf, "Q4b");
    jobB.setJarByClass(Q4.class);
    jobB.setMapperClass(TokenizerMapper2.class);
    jobB.setCombinerClass(IntSumReducer.class);
    jobB.setReducerClass(IntSumReducer.class);
    jobB.setOutputKeyClass(Text.class);
    jobB.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(jobB, new Path(OUTPUT_PATH));
    FileOutputFormat.setOutputPath(jobB, new Path(args[1]));
    System.exit(jobB.waitForCompletion(true) ? 0 : 1);
  }
}
