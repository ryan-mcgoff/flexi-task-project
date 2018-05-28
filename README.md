# FlexiTask 
FlexiTask is a Task managing app that facilitates both fixed and flexi tasks. A fixed task
is simply a task that has a fixed due date, that could be a Birthday on the 21st of Feb or
a bill that needs paid every 2 weeks. A flexitask on the other hand is a recurring Task which 
needs to be completed "roughly" in the time period specified. Perhaps you want to clean the windows
every fortnight, but don't mind if it's a few days late or early. The flexitask app uses a urgency 
sorting algorithm to display the tasks which are MOST overdue (relative to their recurring frequency).
For instance, a yearly task that is 4 days overdue is deemed less overdue than a weekly task which is 2 days
overdue. 

## Installation

### Requirements
* Android Studio (LATEST VERSION)
* Internet connection for first compile
* Pixel phone + API >= 22 for emulator (haven't tested on lower yet or other phone models)



## Algorithm
To determine a flexi-tasks priority the app takes todays date (in milliseconds) and subtracts 
the date the task was created/ last done (in milliseconds) - this represents the milliseconds since the task was last completed
, we then convert this to number of days by dividing by 86,400,000 (milliseconds in a day) and adding 1 (for the due day). Our app then divides that number
by how ofen the task is set to recurre (ie: weekly task, daily task). The end result is a number that represents a percentage of how
complete/overdue/underdue a task is. For tasks that have a lower recurring frequency (ie daily), each day that task is overdue will 
increase the result relativily more than a task with a higher frequnecy (ie: yearly)



     Todays Date - Date Created)/86,400,000 + 1 

     -------------------------------------------
	            Recurring Frequency



## Code used
https://github.com/Clans/FloatingActionButton for the floating action button
