## Authorization Service - Admin Client
The admin client is a javascript application that provides a simple way to manage the users, groups and projects that are stored in the authorization services. It is not a capability that is offered by the real authorization service - it is strictly a development tool. It utilizes a set of API endpoints prepended with `/extras`.

## Admin client development
The admin client is built with [Webpack](https://webpack.github.io/) and [React](https://facebook.github.io/react/).

1. Install node (the project has been most recently built and tested with 0.10.40) and npm. Then clone this repo and run `npm install`.
2. You will need to have an instance of the authorization service running and the `API_URL` environment variable set to run the webpack client development server. For example if your auth service backend is running on `http://localhost:8080` the command to start the development server is `API_URL=http://localhost:8080 npm run dev-server`.
3. You can access the client app at `http://localhost:2992/dist`