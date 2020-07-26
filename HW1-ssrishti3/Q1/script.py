import http.client
import json
import time
import timeit
import sys
import collections
from pygexf.gexf import *


#
# implement your data retrieval code here
#determining value of key
try:
    key = sys.argv[1]
except IndexError:
    key = None

connection = http.client.HTTPSConnection('rebrickable.com')
headers = {"Accept": "application/json", "Authorization": "key "+ key}
url = '/api/v3/lego/sets/?ordering=-num_parts&page_size=350&min_parts=1130'
connection.request("GET", url, headers=headers)
response = connection.getresponse()
result = json.loads(response.read().decode())
#


# complete auto grader functions for Q1.1.b,d
def min_parts():
    """
    Returns an integer value
    """
    # you must replace this with your own value
    #found that 1130 returns 286 sets
    return 1130

def lego_sets():
    """
    return a list of lego sets.
    this may be a list of any type of values
    but each value should represent one set

    e.g.,
    biggest_lego_sets = lego_sets()
    print(len(biggest_lego_sets))
    > 280
    e.g., len(my_sets)
    """
    # you must replace this line and return your own list
    data = result['results']
    set_list = [None]*len(data)
    i=0

    for set in data:  #set is a dict
        for key in set: 
            if key=='set_num' or key=='name':   #extracting set_num and name information
                set_list[i] = {'set_num':set.get('set_num'),'name':set.get('name')}    #set_list is a list of dicts
        i=i+1
    
    #return a list of information about top sets
    return set_list


def gexf_graph():
    """
    return the completed Gexf graph object
    """
    # you must replace these lines and supply your own graph
    set_list = result['results']

    part_data_for_sets = [None] * len(set_list)
    i=0
    for set in set_list:
        set_num=set.get('set_num')
        url_for_set_parts = 'https://rebrickable.com/api/v3/lego/sets/{0}/parts/?page_size=1000'.format(set_num)
        connection.request("GET", url_for_set_parts, headers=headers)
        response = connection.getresponse()
        data_for_set = json.loads(response.read().decode())
        part_data_for_sets[i] = data_for_set['results']
        i=i+1
    #part_data_for_sets is a list of part info for each set

    my_gexf = Gexf("ssrishti3", "Sets and Parts")
    graph = my_gexf.addGraph("undirected", "static","")
    attr = graph.addNodeAttribute('Type',defaultValue='',type="string")

    #adding sets as nodes
    for sets in set_list:
        id = sets.get('set_num')
        label = sets.get('name')
        if graph.nodeExists(id):
            continue
        else:
            node = graph.addNode(id,label,r='0',g='0',b='0')
            node.addAttribute(attr, "set")     

    #add parts as nodes
    for set_data in part_data_for_sets:
        sorted(set_data, key = lambda i:(i['quantity']),reverse=True)
        set_size = min(20,len(set_data))
        del set_data[set_size:]
        for part in set_data:
            partno=part.get('part')
            part_num = partno.get('part_num')
            part_color=part.get('color')
            part_rgb=part_color.get('rgb')
            part_id=part_num + '_' + part_rgb
            part['part_id'] = part_id
            #part_id=part.get('part_id')
            #print(id)
            #temp=part.get('part')
            label=partno.get('name')
            RGB = tuple(int(part_rgb[i:i+2], 16) for i in (0, 2, 4))
            if graph.nodeExists(part_id):
                continue
            else:
                node = graph.addNode(part_id,label,r=str(RGB[0]),g=str(RGB[1]),b=str(RGB[2]))
                #print(node)
                node.addAttribute(attr, "part")

    #add edges to the graph
    j=0
    for sets,set_data in zip(set_list,part_data_for_sets):
        set_id = sets.get('set_num')    
        for part in set_data:        
            part['part_id'] = part_id
            part_id=part.get('part_id')
            #print(set_id,part_id)
            edge_id = j
            edge_weight = part.get('quantity')
            edge = graph.addEdge(edge_id,set_id,part_id,weight=edge_weight) 
            j=j+1

    output_file=open("bricks_graph.gexf","wb")
    my_gexf.write(output_file)

    my_gexf = Gexf("author", "title")
    gexf.addGraph("undirected", "static", "I'm an empty graph")
    return my_gexf.graphs[0]

# complete auto-grader functions for Q1.2.d

def avg_node_degree():
    """
    hardcode and return the average node degree
    (run the function called “Average Degree”) within Gephi
    """
    # you must replace this value with the avg node degree
    return 8.457

def graph_diameter():
    """
    hardcode and return the diameter of the graph
    (run the function called “Network Diameter”) within Gephi
    """
    # you must replace this value with the graph diameter
    return 8

def avg_path_length():
    """
    hardcode and return the average path length
    (run the function called “Avg. Path Length”) within Gephi
    :return:
    """
    # you must replace this value with the avg path length
    return 3.989


