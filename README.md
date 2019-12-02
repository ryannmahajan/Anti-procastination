# Sam
Android App that uses punishments to help users avoid procrastination. 

**Behind the scenes**

When a task is added, a corresponding job is also scheduled for the task's deadline. The job, when running, does one of two things:
1. If the task has been marked complete, the job deletes itself
2. If not, the punishment is carried out by blocking the user out of certain apps using a custom accessibility service triggered on window changes
