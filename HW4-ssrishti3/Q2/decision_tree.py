from util import entropy, information_gain, partition_classes
import numpy as np 
import ast
import numbers

class DecisionTree(object):
    def __init__(self):
        # Initializing the tree as an empty dictionary or list, as preferred
        #self.tree = []
        #self.tree = {}
        # pass
        self.tree = {}
        # self.leaf_size = 5

    def learn(self, X, y):
        # TODO: Train the decision tree (self.tree) using the the sample X and labels y
        # You will have to make use of the functions in utils.py to train the tree
        
        # One possible way of implementing the tree:
        #    Each node in self.tree could be in the form of a dictionary:
        #       https://docs.python.org/2/library/stdtypes.html#mapping-types-dict
        #    For example, a non-leaf node with two children can have a 'left' key and  a 
        #    'right' key. You can add more keys which might help in classification
        #    (eg. split attribute and split value)
        # pass

        #only 1 row
        # if len(X)<=self.leaf_size:
        #     return np.array([['Leaf',y[0],-1,-1]])

        #if no data
        if len(y) == 0:
            self.tree['node_type'] = 'No-data'
            return

        #if all y are same, return a leaf node which predicts that value as output
        if y[1:]==y[:-1]:
            self.tree['node_type'] = 'Leaf'
            self.tree['label'] = y[0]
            return

        #if all x are same, return a leaf node which predicts majority y as output
        if X[1:] == X[:-1]:
            count = 0
            for i in y:
                if i==0:
                    count+=1
            if count>= (len(y) - count):
                op = 0
            else:
                op = 1  
            self.tree['node_type'] = 'Leaf'
            self.tree['label'] = op
            return
            

        #determine split attribute
        split_attr = 0
        split_val = np.mean([row[0] for row in X])
        info_gain = -1

        for i in range(len(X[0])):
            sv = np.mean([row[i] for row in X])
            x_left, x_right, y_left, y_right = partition_classes(X,y,i,sv)
            ig = information_gain(y,[y_left,y_right])
            if ig>info_gain:
                info_gain = ig
                split_attr = i
                split_val = sv

        # print(split_attr,split_val)

        #build tree
        x_left, x_right, y_left, y_right = partition_classes(X,y,split_attr,split_val)

        left_tree = DecisionTree()
        left_tree.learn(x_left,y_left)
        right_tree = DecisionTree()
        right_tree.learn(x_right,y_right)

        self.tree['node_type'] = 'non-leaf'          
        self.tree['split_attr'] = split_attr
        self.tree['split_val'] = split_val
        self.tree['lchild'] = left_tree
        self.tree['rchild'] = right_tree

        return

    def classify(self, record):
        # TODO: classify the record using self.tree and return the predicted label
        # pass

        if self.tree['node_type'] == 'Leaf':
            return self.tree['label']

        if self.tree['node_type'] == 'No-data':
            return 0

        sa = self.tree['split_attr']
        sv = self.tree['split_val']

        if record[sa] <= sv:
            return self.tree['lchild'].classify(record)
        else:
            return self.tree['rchild'].classify(record)
