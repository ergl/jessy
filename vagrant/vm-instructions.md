# Using rubis and jessy from Vagrant

## Installation

- Download and install [Virtualbox](https://www.virtualbox.org/wiki/Downloads) and [Vagrant](https://www.vagrantup.com/downloads.html)

- Clone the [jessy fork](https://github.com/ergl/jessy)

## Usage

Inside the jessy project, `cd` into the `vagrant/rubis` folder

```bash
└── rubis
    ├── Vagrantfile  # Describes the vm configuration
    ├── build.sh  # Downloads jessy into the vm
    ├── client-vm.sh  # Spawns a vm with the client
    ├── configure-network.sh  # Configures fractal (gets the ip from the server and copies to the clients)
    ├── provision.sh  # Downloads git, curl, jdk to the virtual machine
    └── server-vm.sh  # Spawns a vm with the server
``` 

You can forget about Vagrantfile, and {provision,build,configure-network}.sh files for now. You can (hopefully!) do everything using just `client-vm` and `server-vm`. Their usage is as follows:

```bash
# These commands may take a few minutes on the first run, while it downloads everything. Subsequent runs should be faster

# Creates, provisions and connects to a server vm
./server-vm.sh

# Creates, provisions and connects to a client vm
./client-vm.sh
```

You can also use vagrant directly. Read the [vagrant docs](https://www.vagrantup.com/docs/) for more.

```bash
# (Create | Reload | Shut down | Destroy) the (server | client) vm
vagrant (up | reload | halt | destroy) (server | client)
# ssh into the appropiate vm
vagrant ssh (client | server)
```

## Running jessy

After executing either the `*-vm.sh` scripts, or launching manually and connecting through ssh to the vms , you can run the project as usual:

```bash
# On the server vm
cd ~/jessy/script
./rubis-server.sh [debug]

# On the client vm
cd ~/jessy/script
./rubis-init.sh [debug]
./rubis-client.sh [debug]
```

## Debugging

In all cases, passing the `debug` flag to the scripts will let you attach a debugger to the application. In all cases, the jvm will wait for a debugger before running the application. (You can configure the behavior by editing the jvm flags on those files).

To remotely connect a debugger (using intellij, other environments might be different):

- Run > Edit Configurations
- Click `+` and `Remote`
- Fill in the details, and click apply

For the server, under `Host`, change the port to `5006`. For the client, you can leave it in `5005`.

## Configuration

By default, the vms come with two processors and 1024M of memory. To change that, you can edit the `Vagrantfile`, and change the `config.vm.provider` settings to something else.

All the vms have a shared folder on `/srv`. Inside the vm, you can `cd /srv` to access the shared folder. This is how the different machines share the network configuration.

If ports `5005` and `5006` are being used in your system by other applications, you can change the defaults to something else. To do so, edit the `Vagrantfile`:

```ruby
# Change the "host" port to whatever value you want.
# The "guest" port should be ok, nothing else is running in the vm
...
config.vm.define "server" do |server|
        hostname = "jessy-server"
        server.vm.network "forwarded_port", guest: 5005, host: 5006
...

# Do the same to the "client" vm
...
config.vm.define "client" do |client|
        hostname = "jessy-client"
        client.vm.network "forwarded_port", guest: 5005, host: 5005
...
```
