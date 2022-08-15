# SmartNotifcation [MVVM / Room / coroutine / RecyclerView]

- This app use a background service to get the system notifications, collecting package name, title, content and post time from notifications.
- Use MVVM to separate data presentation logic from business logic by moving it into particular class for a clear distinction .
 
 

#Demo
<table>
    <thead>
        <tr>
            <th>Pic</th>
            <th>Discription</th>
        </tr>
    </thead>    
    <tbody>
        <tr>
            <td>![request](https://user-images.githubusercontent.com/26586409/184670272-0383c3a3-2a0d-4e8c-9271-af8e931a4c22.jpg)</td>
            <td>A dialog for requesting the permission to access the system notification.</td>
       </tr>       
        <tr>
            <td>![allowPermission](https://user-images.githubusercontent.com/26586409/184670426-d64c18cd-21a1-4f09-8325-09fe6af4a16d.jpg)</td>
            <td>Navigate to the permission settings page.</td>
        </tr>        
        <tr>
            <td>![notifications](https://user-images.githubusercontent.com/26586409/184670480-f1258cdb-db68-4778-a62d-9c19e681d355.jpg)</td>
            <td>Show permissions.</td>
        </tr>
        <tr>
            <td></td>
            <td>Delete notification from database.</td>
        </tr>
        <tr>
            <td></td>
            <td>Delete all.</td>
        </tr>                
    </tbody> 
</table>

## Structure

![classDiagram](./pic/classDiagram.png)
