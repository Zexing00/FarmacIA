# Sample AGENTS.md 

## Build overview 
  - We are developing an Android application in Java.
  - When the user opens the application, they will see a screen with two buttons: one for logging in and another for registering. The user’s data will be stored in a table in an SQL database.
  - Once they have logged in or registered, they will be taken to the next screen, where they will have two buttons: one to view their personal pillbox, and another to query the SQL database with a large list of registered   medications.
  - The model consists of a digital “pharmacy”; the user can consult the leaflet/information for any pill or medication they want.
  - Each user has their own digital “pill box”, where they can enter the medication they are currently taking.
  - A user can grant permission to another user to edit their pill box; the user with extra permissions is then treated as an Admin.
  - The user can also search medications with a search bar, and a list of matching medications will be shown.
    
## Restrictions
  - Use of ORMs is forbidden
  - Must use DAOs for database access
  - Each activity may have its own design file in XML
  - Add libraries when needed
  - 
## Meta instructions
  - You must iterate and keep iterating until the problem is completely solved.
  - Before applying code changes, you must identify a sequence of specific, simple, and verifiable steps for the problem you have been given.
  - Before editing, review the relevant files to ensure that you have the full context of the problem.
  - Only make code changes when there is absolute confidence that they solve the problem.
