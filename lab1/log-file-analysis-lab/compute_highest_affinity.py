# No need to process files and manipulate strings - we will
# pass in lists (of equal length) that correspond to 
# sites views. The first list is the site visited, the second is
# the user who visited the site.

# See the test cases for more details.
import collections
 
def highest_affinity(site_list, user_list, time_list):

  #Converting the sitelist into a Set to get the Uniquer Set of Sites
  site_set = set(site_list) 
  
  site_dict = {}
  #Iterate through the Set to get the Uniquer value and get the count from the List
  for item in site_set:
    site_dict[item] = site_list.count(item)
  
  #Create a new Dict wih the Key Value pair of the Site
  value_to_key = collections.defaultdict(list)
  for k, v in site_dict.iteritems():
    value_to_key[v].append(k)

  pair = ("null", "null")
  
  #Get the First Term with the Max Value
  maxValue1 = max(value_to_key.keys(), key=int)
  listOfMaxItem1 = value_to_key[maxValue1]
  tmpItem1 = "" 
  item1 = ""
  if len(listOfMaxItem1) == 1:
    tmpItem1 = value_to_key[maxValue1]
    item1 = tmpItem1[0]
  elif len(listOfMaxItem1) > 1:
    for maxItem1 in range(len(listOfMaxItem1)):
      if tmpItem1 == "" :
        tmpItem1 = listOfMaxItem1[maxItem1]
      else :
        if tmpItem1 > listOfMaxItem1[maxItem1]:
          tmpItem1 = listOfMaxItem1[maxItem1]
      
      #Set the Calculated Item of Highest Affinity as Item 1
      item1 = tmpItem1

  #Deleting the Item already used (Highest possible affinity) from the Dictionary to avoid future conflicts
  if(len(listOfMaxItem1)) == 1:
    #Delete the Value from the Dict
    del value_to_key[maxValue1]
  elif(len(listOfMaxItem1)) > 1:
    values = listOfMaxItem1


  #Get the Second Item with the Max Value
  maxValue2 = max(value_to_key.keys(), key=int)
  listOfMaxItem2 = value_to_key[maxValue2]
  listOfMaxItem2.sort()
  item2 = ""

  #If Length of List itself is one, then we have the second highest affinity value. Use it as is.
  if len(listOfMaxItem2) == 1:
    tmpItem2 = value_to_key[maxValue2]
    item2 = tmpItem2[0]
  elif len(listOfMaxItem2) > 1: #If greater than 1, then iterate to find the value with the highest affinity
    tmpItem2 = "" 
    for maxItem2 in range(len(listOfMaxItem2)):
      if tmpItem2 == "" :
     	  tmpItem2 = listOfMaxItem2[maxItem2]	
      else :
        #Keep Adjusting the value of the Temp Items depending on whether its higher than the value it is being checked against
         if tmpItem2 > maxItem2:
      	   tmpItem2 = listOfMaxItem2[maxItem2]
 
      #After exhausting all options, the value remaining in tmpItem2 is the one with the highest affinity
      #Set it to Item 2       
      item2 = tmpItem2

  #Since A List has to be returned, set Item 1 and Item 2 calculated into the List
  a = []
  a.insert(0, item1)
  a.insert(1, item2)

  #Convert the List to a Tuple. This is needed in order to correct the combination of values
  #It has to be placed in an Alphabetical Order
  pair = tuple(a)
  if pair[0] > pair[1]:
   pair = (pair[1], pair[0]) 
 
  print "Highest Affinity Pair is: ", pair

  #Return the Highest Affinity Pair
  return pair