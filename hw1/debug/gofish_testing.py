import gofish1

deck1 = [ ( "3", "hearts" ) ]
hand1 = { "2" : [ "hearts", "spades" ] }

gofish1.drawCard("Test1", deck1, hand1)


deck2 = [ ( "3", "hearts" ) ]
hand2 = { "2" : [ "hearts", "spades" ],
          "3" : [ "diamonds" ] }

gofish1.drawCard("Test2", deck2, hand2)

deck3 = [ ( "3", "hearts" ) ]
hand3 = { "2" : [ "hearts", "spades" ],
          "3" : [ "diamonds", "clubs", "spades" ] }

gofish1.drawCard("Test3", deck3, hand3)

deck4 = [ ( "3", "hearts" ) ]
hand4 = { "2" : [ "hearts", "spades" ],
          "3" : [ "diamonds", "clubs", "spades", "hearts" ] }

gofish1.drawCard("Test4", deck4, hand4)


deck5 = [ ( "3", "hearts" ) ]
hand5 = { "4" : [ "hearts", "spades" ],
          "5" : [ "diamonds" ], 
          "6" : [ "clubs" ], 
          "7" : [ "spades" ] }

gofish1.drawCard("Test5", deck5, hand5)

deck6 = [ ( "6", "hearts" ) ]
hand6 = { "3" : [ "hearts" ],
          "4" : [ "diamonds" ], 
          "5" : [ "clubs" ], 
          "6" : [ "spades" ] }

gofish1.drawCard("Test6", deck6, hand6)

