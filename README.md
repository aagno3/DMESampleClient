# DMESampleClient
This project runs with DMESampleSever project (https://github.com/aagno3/DMESampleServer)

DMESampleServer => maintain file on local host.
DMESampleClient => client run this program to write on file server by executing DME algorithm on its end.

Command 
post <msg> => this will write msg on file on server
view => this will fetch all msg from file
  
Steps to run DMESampleClient on client side
1) port and local ips of all client including itself are hardcoded in UserApplication so change as per your convenient.
2) Run this program by passing port and name of client eg java UserApplication 3000 Amitosh
3) Now pass command eg post Hello or view
