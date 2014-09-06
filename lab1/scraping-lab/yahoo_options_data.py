import json
import sys
import re
import urllib

from bs4 import BeautifulSoup
from operator import itemgetter 

def contractAsJson(filename):
  jsonQuoteData = "[]"

  '''
  Start
  '''

  #Open the File based on the File Name passed
  print "File Name Input is: ", filename
  fileData = open(filename)

  #Using BeautifulSoup, read the Contents of the FILE into the soup variable
  print "Pass the File Data into the Soup"
  soup = BeautifulSoup(fileData)
  print "---------------------------------------------------------------------"
  print soup.prettify()
  print "---------------------------------------------------------------------"
 
  #Figure out the name of the Company whose Data is being processed
  print "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
  rawCmpyName = soup.find("div", ("class", "title")).text
  print "Company Name is: ", rawCmpyName
  dummyA, initialCmpnyName = rawCmpyName.split("(")
  finalCmpnyName, dummyD = initialCmpnyName.split(")")
  print "Final Company Name is now: ", finalCmpnyName
  print "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"


  #Find out the Price of the Stock for the Company
  #Use the combination of a Company Name and the Unique Tag associated with that field
  print "#####################################################################"
  nameSpan = soup.find_all("span", {"class", "time_rtq_ticker"})
  #print "The Name Span retrieved is: ", nameSpan
  id_value = "yfs_l84_" + finalCmpnyName.lower()
  print "ID Value is: ", id_value
  price = soup.find("span", id=id_value).string
  print "The Price is:", price
  print "#####################################################################"
  


  #Find out all the Details related to the URL's that are assigned for Expiration
  print "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
  #uniqueExpirationUrl = "?s=AAPL&m=" - Sample
  uniqueExpirationUrl = "?s=" + finalCmpnyName + "&m="
  print "uniqueExpirationUrl is: ", uniqueExpirationUrl
  
  listOfDates = []
  #Getting all the Links in the Page with the "a" tag
  for link in soup.find_all('a'): 
    linkHref = link.get('href') #Read the Href value for the Tag
    if uniqueExpirationUrl in linkHref: #Check if the Expiration URL exists
      #Add it to the List if it exists
      listOfDates.append(linkHref.replace("&", "&amp;"))
  
  #Create the URL List for Expiration section
  expirationDateUrlList = []
  for expirationDate in listOfDates:
    expirationUrl = "http://finance.yahoo.com" + expirationDate
    expirationDateUrlList.append(expirationUrl)
  print "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"


  #Find out the Data Tables that exist in the Screen
  print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
  tables = soup.find_all("table" , ("class", "yfnc_datamodoutline1"))
  listOfTables = []
  #Iterate through the Returned Tables to find the Inner tables and add it a separate list
  for indTable in tables:
    innerTable = indTable.find("table")
    listOfTables.append(innerTable)

    
  #Iterate through the newly created List with the Inner Tables    
  tblList = []
  for table in listOfTables:
    #Find all the Table Rows in the Tables that are being processed
    tableRows = table.find_all("tr")
    
    rowList = []
    #For each table, iterate through all its rows
    for tblRow in tableRows:
      #Find all tableData that correspond to each Row that is processed. 
      tblRowTblDataList = tblRow.find_all("td", ("class","yfnc_tabledata1"))
      tblRowFncList = tblRow.find_all("td", ("class","yfnc_h"))
      #Create a Master list of all the "td" in the table rows
      tblRowDataList = tblRowFncList + tblRowTblDataList

      #If the row is empty, don't process it. Continue to the next row
      if not tblRowDataList:
        continue;

      tdList = []
      print "-------------------------------------"
      for tblRowData in tblRowDataList:
        print "Data Contained is: ", tblRowData.text
        tdList.append(str(tblRowData.text))
   
      print "-------------------------------------" 
      tblList.append(tdList)
  print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"

  #Once all valid "td"s are populated into the list, iterate through them to pull the desired information
  optionQuotesList = []
  #Fields of individual option quotes
  individualOptionQuotes = {
                          "Ask" : "",
                          "Bid"  : "",
                          "Change" : "",
                          "Date" : "",
                          "Last" : "", 
                          "Open" : "",
                          "Strike" : "",
                          "Symbol" : "",
                          "Type" : "",
                          "Vol" : ""
                        }
  
  #Iterate through the data list that was created in the previous section
  for tblData in tblList:
    #Create a Dict for each TD that is being processed
    tmpDataDict = {}
    count = 0 #Using counter to place data for required fields at required loop
    for data in tblData:
      
      if count == 0: 
        #Strike is always at Position 0
        tmpDataDict["Strike"] = data
      elif count == 1: 
        #Position 1 contains the data for fields, Symbol, Date and Type
        #Split based on the Length of the Company Symbol.
        #In case of AAPL, there is an extra symbol possible AAPL7, so handle that by increasing length by 1
        if "AAPL7" in data: 
          lengthOfCompany = lengthOfCompany + 1
        else:
          lengthOfCompany = len(finalCmpnyName)
        
        #Set the Symbol, Date and Type based on the Length of the Company Symbol
        tmpDataDict["Symbol"] = data[0:lengthOfCompany]
        tmpDataDict["Date"] = data[lengthOfCompany:lengthOfCompany+6]
        tmpDataDict["Type"] = data[lengthOfCompany+6]

      elif count == 2:
        #Position 2 contains the data for "Last"
        tmpDataDict["Last"] = data
      elif count == 3:
        #Position 3 contains the data for "Change"
        tmpDataDict["Change"] = data
      elif count == 4: 
        #Position 4 contains the data for "Bid"
        tmpDataDict["Bid"] = data
      elif count == 5:
        #Position 5 contains the data for "Ask"
        tmpDataDict["Ask"] = data
      elif count == 6:
        #Position 6 contains the data for "Vol"
        tmpDataDict["Vol"] = data
      elif count == 7:
        #Position 7 contains the data for "Open"
        tmpDataDict["Open"] = data
      
      #Maximum number of fields is 7. Reset the counter and loop again for next TD
      if count == 7: 
        #Append the Dict created so far to the list
        optionQuotesList.append(tmpDataDict)
      
      #Incrementing the Counter Now. DO this before looping back. Once loop is done, it will be reset to 0 anyways
      count = count + 1 

  #Sort the Options Quote List of Dicts based on the "Open" key in the descending order
  sortedOptionQuotesList = sorted(optionQuotesList, key=lambda k: int(k["Open"].replace(",","")), reverse=True)
  

  #Take all the data retrieved so far and put in a JSON object
  print "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"
  print json.dumps(
    {
      "currPrice" : float(price),
      "dateUrls"  : expirationDateUrlList,
      "optionQuotes" : sortedOptionQuotesList
    },
    sort_keys=True,
    indent=4, 
    separators=(',', ': ')
  )


  #Set the Data calculated so far into the required JSON Fields 
  jsonQuoteData = json.dumps({
                    "currPrice" : float(price),
                    "dateUrls"  : expirationDateUrlList,
                    "optionQuotes" : sortedOptionQuotesList
                  })

  print "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"

  '''
  End
  '''
  
  return jsonQuoteData