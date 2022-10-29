package fragmentRentalTabs

data class Rental(var customerId:String?=null,var dueDate:String?=null,var rentDate:String?=null,var returnDate:String?=null,
                  var status:String?=null,var totalPayment:Float?=null)