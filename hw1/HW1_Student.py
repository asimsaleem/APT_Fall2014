#Student Class
class Student(object):

	#Defining the Init Method
	def __init__(self, name, gpa, age):
		self.name = name
		self.gpa = gpa
		self.age = age

	#__str__()
	def __str__(self):
		print "_str__ Invoked"
		return "Data is: {" + str(self.name) + "," + str(self.gpa) + "," + str(self.age) + "}"

	#__lt__
	def __lt__(self, other):
		print "__lt__ Invoked"
		return self.gpa < other.gpa

	#__eq__()
	def __eq__(self, other):
		print "__eq__ invoked"
		return ((self.name.lower(), self.gpa, self.age) == 
				(other.name.lower(), other.gpa, other.age))

	#__hash__()
	def __hash__(self):
		print "__hash__ invoked"
		return hash(self.name and self.gpa and self.age)


	def __cmp__(self, other):
		print "__cmp__ invoked"
		if self.gpa < other.gpa:
			return self.gpa
		else: 
			return cmp(self.gpa, other.gpa)

	def __repr__(self):
		return '{}: {} {} {}'.format(self.__class__.__name__,self.name,self.gpa,self.age)


##################################

print "--------------------------------"
#Dict Function Test with the Student Object
print "DICT TEST"
student0_1 = Student("Adam", 4.0, 25)
print "Dict Test Result: ", student0_1.__dict__
print "--------------------------------"

print "--------------------------------"
#Hash Function Test with Sorted:
print "HASH TEST"
studentList_1 = [	
				Student("Bart", 2.0, 21),
				Student("Carl", 3.0, 23),
				Student("Adam", 4.0, 25)
			  ]
print "Hash Test Result: ", sorted(studentList_1, key=Student.__hash__)
print "--------------------------------"


print "--------------------------------"
#Sort Function Test:
print "SORT TEST"
studentList_2 = [Student("Bart", 2.0, 21),
			     Student("Carl", 3.0, 23),
			     Student("Adam", 4.0, 25)]

print "Sort Test Result: ", studentList_2.sort()
studentList_2.sort()
studentSortResult = ""
for student in studentList_2:
  studentSortResult += "%s "  % student
print studentSortResult
print "--------------------------------"


print "--------------------------------"
#Test Case for String:
print "STRING TEST"
studentString = Student("Adam", 4.0, 25)
print "String Test Result: ", str(studentString)
print "--------------------------------"


#Test Case for LESS THAN Test:
print "--------------------------------"
print "LESS THAN TEST"
student1_1 = Student("Bart", 3.0, 23)
student1_2 = Student("Adam", 4.0, 25)
print "Expecting FALSE. Result is: ", student1_2 < student1_2
print "Expecting TRUE. Result is: ", student1_1 < student1_2
print "--------------------------------"

print "--------------------------------"
#Test Case for EQUAL Test: 
print "EQUAL TEST"
student3 = Student("Bart", 3.0, 23)
student4 = Student("Adam", 4.0, 25)
student5 = Student("Adam", 4.0, 25)
print "Expecting FALSE. Result is: ", student4 == student3
print "Expecting TRUE. Result is: ", student4 == student5
print "--------------------------------"
