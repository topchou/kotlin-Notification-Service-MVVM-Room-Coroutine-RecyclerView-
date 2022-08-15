# SmartNotifcation [MVVM / Room / coroutine / RecyclerView]

- This app use a background service to get the system notifications, collecting package name, title, content and post time from notifications.
- Use MVVM to separate data presentation logic from business logic by moving it into particular class for a clear distinction .
 
## Demo

| Pic | Discription |
| --- | ----------- |
| <img src="https://user-images.githubusercontent.com/26586409/184670272-0383c3a3-2a0d-4e8c-9271-af8e931a4c22.jpg" width="200" height="400" /> | A dialog for requesting the permission to access the system notification. |
| <img src="https://user-images.githubusercontent.com/26586409/184670426-d64c18cd-21a1-4f09-8325-09fe6af4a16d.jpg" width="200" height="400" /> | Navigate to the permission settings page. |
| <img src="https://user-images.githubusercontent.com/26586409/184670480-f1258cdb-db68-4778-a62d-9c19e681d355.jpg" width="200" height="400" /> | Show notifications. |
| <img src="https://user-images.githubusercontent.com/26586409/184673200-953984df-efbd-4432-ba57-b2f04d2ca137.jpg" width="200" height="400" /> | Delete notification from database. |
| <img src="https://user-images.githubusercontent.com/26586409/184673178-e02f9edc-e2a3-4580-86e0-07c7dfac6a41.jpg" width="200" height="400" /> | Delete all notifications from database. |

## Project Structure

![classDiagram](./pic/classDiagram.png)
