#Create an Array of Five Students
#Name, Age, GPA - Unique GPAs
'''
testArray = [["Adam", 25, 3.0],
 			 ["Carl", 24, 4.0],
 			 ["Bart", 23, 3.5],
 			 ["Deng", 22, 2.5],
 			 ["Eden", 21, 2.0]]
'''

#Name, Age, GPA - GPA gets repeated. Sorting on Name
'''
testArray = [["Adam", 25, 3.0],
 			 ["Bart", 24, 4.0],
 			 ["Carl", 23, 4.0],
 			 ["Deng", 22, 2.0],
 			 ["Eden", 21, 2.0]]
'''

#Name, Age, GPA - GPA gets repeated. Sorting on Name and Age
testArray = [["Adam", 25, 3.0],
 			 ["Bart", 24, 4.0],
 			 ["Carl", 23, 4.0],
 			 ["Deng", 22, 2.0],
 			 ["Deng", 21, 2.0]]

#Sort by GPA in Increasing Order. Implies that the Lowest GPA should come first
sorted_list = sorted(testArray, key=lambda x:(x[2], x[0], x[1]))

print "Test Array is: ", testArray
print "Sorted List is: ", sorted_list