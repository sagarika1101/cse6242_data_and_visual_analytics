package edu.gatech.cse6242;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.util.*;
import java.io.*;

public class Q1 {
  
  public static class TokenizerMapper
       extends Mapper<LongWritable, Text, Text, Text> {

    private Text source = new Text();
    private Text target_weight = new Text();
    
    public void map(LongWritable key, Text value, Context context
                    ) throws IOException, InterruptedException {

      String line[] = value.toString().split("\t");
      
      source.set(line[0]);
      target_weight.set(line[1] + " " + line[2]);
      context.write(source, target_weight);
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
    private Text result = new Text();

    public void reduce(Text key, Iterable<Text> values, Context context
                       ) throws IOException, InterruptedException {

      int global_target = -1; 
      int global_weight = -1;
      for (Text val : values) {
          String target_weight[] = (val.toString()).split(" ");
	  int curr_target = Integer.parseInt(target_weight[0]);
          int curr_weight = Integer.parseInt(target_weight[1]);
          if(curr_weight > global_weight) {
              global_weight = curr_weight;
              global_target = curr_target;
          }
      }
      if(global_weight > 0)
        result.set(global_target + "," + global_weight);
      context.write(key, result);
    	
   
    }
  }
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Q1");

    /* TODO: Needs to be implemented */
    job.setJarByClass(Q1.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setReducerClass(IntSumReducer.class); 
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
