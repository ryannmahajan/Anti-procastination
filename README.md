# Sam (Anti-Procastination App)
Android App that uses punishments to help users avoid procrastination. Click [here](https://play.google.com/store/apps/details?id=com.ryannm.android.sam) to check it out. 


## Screenshots

<img src="https://user-images.githubusercontent.com/22999944/112921321-2e574280-90d0-11eb-8910-98398bfe5bbc.png" alt="Screenshot 1"/>        <img src="https://user-images.githubusercontent.com/22999944/112921347-3b743180-90d0-11eb-931d-a59aa826ff43.png" alt="Screenshot 2"/>        <img src="https://user-images.githubusercontent.com/22999944/112921354-3e6f2200-90d0-11eb-8ea5-af9b2a542a29.png" alt="Screenshot 3"/>


## Behind the scenes

When a task is added, a corresponding job is also scheduled for the task's deadline. The job, when running, does one of two things:
1. If the task has been marked complete, the job deletes itself
2. If not, the punishment is carried out by blocking the user out of certain apps using a custom accessibility service triggered on window changes
