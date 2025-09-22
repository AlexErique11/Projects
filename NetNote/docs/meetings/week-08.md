## Agenda week 8

Date: 15/01/2025 \
Time: 14:45 \
Location: Drebbelweg PC hall 3 \
Chair: Maksymilian Maciejek\
Minute Taker: Julian Bladergroen \
Attendees:  6 + TA


- Opening by chair (1 min)
    - Next week is the last Meeting.
- Announcments by TA (5 min)
    - Feedback was released.
    - One of the non-functional requirements is to have a config file. (We already have a config for language) In this file we should keep track of all client preferences in JSON format. Upon startup, get all collections from the server; as a user you can decide to change the local name and delete the collection you do not want to see. This has to be kept in the config file, so it can be reloaded on the next startup. 
    - If next week's Knockout criteria are not met, you will receive a warning on monday.

- Showing our app with the bunch of new features like: (6min)
    translations, undoActions, Keyboard Shortcuts and navigation, multiple servers

### Updates from last week (11 minutes)
- Discuss the feedback that we have gained since our last meeting. (5min)
  Usability/Accessibility -> we got 3/9
    Implemented Features -> 7/10, 6/6 for basic requirements and only 1 point for the rest
    - Has everyone seen the feedback?
    - We still have to do the CSS file for the webview (basic requirement): Create the style options for the webview. You should be able to switch the style. There is a default css file which has a few themes, and the user has the ability to directly change the hardcoded contents of that file. This file should be connected to the webview.
    - This week: create an "ALL" option for collections, the config file for this might be tricky. If the user selects one of the collections as default, it should open again on startup. 
    - For **embed** we didn't get all the points, Alex will try and implement this. (For example; we should be able to move a file from one note to another within the same collection).
    - **Interconnected content**: Daniel and Kaan fixed the "referring to different note" bug.
    - **Live language switch** was just implemented.
    - **Multi modal Visualisation** and **Keyboard Navigation** are kind of implemented. For Multi-modal the language flags also count as a single implementation, so we should add another implementation.
    - **Undo Action**: Have a popup of the undo actions, and once you move to another note the undo actions shouldn't be saved every time you switch a note; so ignore all past actions of that note.
    - **Error messages** should be given through a popup. (Like if you have a file with the same name you should get a popup.)

- Discuss the issues from the last week. How people feel they did?  (6min)
    What do we have to fix? What can we add to the app?
    - Does everyone know what to do?
    - Configuration file should contain Color, language, font size... etc. It Shouldn't save local names of the notes, but should save local names of Collections. You should be able to delete collections locally but not from the server, and change the name locally. Daniel: We should probably focus on other stuff because collections is only worth 0-1 point(s).
    - Filtering implementation: The content should not be translated, but the errors like "not found" should be, while it is not necessary for basic requirements, though it is sufficient for receiving more points.
    


### Talking Points (22 min)

- Does everyone know what they have to do this week? (3 min)
    Is everyone able to finish the work for this week?
    Does anyone need extra help?

- Questions to the TA:
  - What exactly should we do with the CSS file for the WebView, there is still some uncertainty. (0 min)
    - Question was answered before, so it was not asked. 
  - "The configured collections are stored in a local config file, so they are persisted across restarts. " - what does it exactly mean? (0min)
    - Also answered before

-To use [[other note]] to refer to other notes in the same collection by title, so I can link related notes (0min) (not asked)


- Refactoring and injections, how are we handling this? Are we choosing a specific time slot for Julian to do this??
    We don't want to finish like in the last week, should all merge request be tested and approved
    before the refactoring? (5min)
    - Thursday 16th refactoring can be done using strong injection.
    - Number of merge request shouldn't be an issue. Week 4-5 is technically counted as 2 weeks. For this week we need around 10 merge requests more. For an "excellent grade" we need at least 2 merge request per week per person.

- The number of issues per person - correction (2min)

- How are we handling the list of bugs that should be fixed? (2min)
    - List of bugs, what are we doing with it? If someone has free time you should make an issue with a bug and try and fix it, as there is only around 10 and they are small. Next week the people who want to can start doing this.
    

- Any other ideas how can we upgrade our Project? (2min)
    - Do we want to have extra upgrades? Setting sbutton is like 10 lines. Alex has time to do the css file, and moving the embedded files. Marcin: The tags overlap with the text field when a lot of tags are added.
    - We can do rescaling by adding extra space, you could also use computed size. Rescaling the app is not graded, so we shouldn't spend too much time on this. 

- Who need some extra lines for either client or the server: discuss and find the solution(2min)
    - Everyone should check if they have 100 lines on client AND on server. Adding tests in the server can fix this. Everyone: Count how many lines they added to each.
    - Final knockout: It counts throughout the whole project, so you are safe when you created something earlier but it got deleted later on.

### Finishing up (9 min)
- Briefly talk about the things we will do in the next week and our goals 
    for finishing up the app. (4min)
    - Maks briefly talked about what is done, not specifically per person.
    - Readme file should be updated on every new feature. We should state everything that is hidden by default in the readme. 
    - Next monday meeting we should explain functionality to eachother (?)
    - Meaningful addition to language feature: Make sure the cut off text on buttons is dealt with when changing the language, for example by resizing.
  
- Any other questions?(4min)(no)

- Wrap up (1 min)
    - Thanks: Ended at 15:31 with a total duration of 46 minutes.

Total time: 46 minutes (44m guessed)