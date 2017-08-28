<!DOCTYPE html>
<html>
    <head>
        <title>Simple Blog Posts</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body>
        <h1>All posts</h1>
        <div>
            <table>
                <thead>
                    <tr>
                        <th> ID</th>
                        <th>Title </th>
                        <th>Content</th>
                    </tr>
                </thead>
                <tbody>
                    <#list posts as e>        
                    <tr>
                        <td>${e.id?html}</td>
                        <td>${e.title?html}</td>
                        <td>${e.content?html}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>  

        </div>
    </body>
</html>
