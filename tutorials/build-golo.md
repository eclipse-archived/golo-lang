# Build Golo within a Virtual Machine

Building **Golo** is something easy:

```shell
git clone https://github.com/eclipse/golo-lang.git
cd golo-lang
./gradlew installDist
```

But sometimes, you don't want to install all the needed tools on your laptop (for good or bad reasons). The goal of this document is to explain how to build **Golo** within a virtual machine.

We'll use **[Multipass](https://multipass.run/)** (from **Canonical**) which exists on the leading platforms (Linux, OSX and Windows). Multipass is a lightweight VM manager, simple and easy to use.

> ✋ **Requirements**:
> - Install Multipass, see the [documentation](https://multipass.run/docs)
> - Ensure `JAVA_HOME` environment variable is set and points to your JDK installation

## Create the VM and build Golo distribution

- First, create a working directory.
- In this directory, create two files: `create-vm.sh` and `cloud-init.yaml`.
- Give the execution rights to `create-vm.sh`.

```shell
touch create-vm.sh
touch cloud-init.yaml
chmod +x create-vm.sh
```

Add this content to `cloud-init.yaml`

```yaml
package_update: true
```

And this is the content of `create-vm.sh`:

```shell
#!/bin/bash

vm_name="golo-lang-vm"
vm_cpus=3
vm_mem="4G"
vm_disk="100GB"

if [ ! -d "golo-distribution" ];then
  mkdir golo-distribution
fi

multipass launch focal --name ${vm_name} --cpus ${vm_cpus} --mem ${vm_mem} --disk ${vm_disk} \
  --cloud-init ./cloud-init.yaml # 1️⃣

multipass mount golo-distribution ${vm_name}:golo-distribution # 2️⃣

multipass --verbose exec ${vm_name} -- sudo -- bash <<EOF
apt install openjdk-8-jdk -y # 3️⃣
apt install -y maven
apt install -y mercurial
EOF

multipass --verbose exec ${vm_name} -- bash <<EOF
git clone https://github.com/eclipse/golo-lang.git
cd golo-lang
./gradlew installDist # 4️⃣
EOF

multipass --verbose exec ${vm_name} -- bash <<EOF
cp -a golo-lang/build/install/. golo-distribution # 5️⃣
chmod -R a+rwx golo-distribution
EOF
```

> - 1️⃣ creation of the virtual machine
> - 2️⃣ mounting of a shared directory (between the host and the vm)
> - 3️⃣ installation of required tools to build Golo
> - 4️⃣ build the Golo distribution
> - 5️⃣ copy the distribution to the shared directory

> You can retrieve the source code here: [resources/build-golo/create-vm.sh](resources/build-golo/create-vm.sh)

Now, run the script `create-vm.sh` and wait a few minutes. A moment later you'll get the golo's distribution 🎉 in the shared `golo-distribution` directory:

```shell
golo-distribution
└── golo
   ├── bin
   │  ├── golo
   │  ├── golo-debug
   │  ├── golo-debug.bat
   │  ├── golo.bat
   │  ├── golosh
   │  ├── golosh.bat
   │  ├── vanilla-golo
   │  └── vanilla-golo.bat
```

The last step is to add the `bin` directory of the created directory `golo-distribution/golo` to the PATH environment variable of you host computer.

For example, from your working directory, you can type the following commands (if you use bash):

```shell
echo "export GOLO_HOME=~/${PWD}/golo-distribution/golo" >> ~/.bashrc
echo 'export PATH=$PATH:$GOLO_HOME/bin' >> ~/.bashrc
source ~/.bashrc
```

Then, check your setup by typing the below command:

```shell
golo version
```

You should get something like this:

```shell
3.4.0-SNAPSHOT
```

### The essential "Hello World!"

Use your favorite code editor ([Micro](https://github.com/zyedidia/micro) is nice, runs in the terminal and Golo support is native), and create a new `hello.golo` file:

```golo
module hello.world

function main = |args| {
  println("Hello world!")
}
```

And run it: `golo golo --files hello.golo`

## Some useful commands for your VM

- Stop the VM: `multipass stop golo-lang-vm`
- Start the VM: `multipass start golo-lang-vm`
- Open a shell on the running VM: `multipass shell golo-lang-vm`
- Remove the VM: `multipass delete golo-lang-vm; multipass purge`

### How to rebuild the distribution

```shell
multipass shell golo-lang-vm
rm -rf golo-lang
rm -rf golo-distribution/golo
git clone https://github.com/eclipse/golo-lang.git
cd golo-lang
./gradlew installDist
cd ~/
cp -a golo-lang/build/install/. golo-distribution
chmod -R a+rwx golo-distribution
```

## [✋ Experimental] Create a VM to "hack" Golo

If you want to modify Golo source code (or test a specific branch), but if you're going to continue using the virtual machine to build the distribution, you can write a script like this one below:

```shell
#!/bin/bash

vm_name="golo-lang-vm"
vm_cpus=3
vm_mem="4G"
vm_disk="100GB"

if [ ! -d "golo-project" ];then
  mkdir golo-project
fi

multipass launch focal --name ${vm_name} --cpus ${vm_cpus} --mem ${vm_mem} --disk ${vm_disk} \
  --cloud-init ./cloud-init.yaml

multipass mount golo-project ${vm_name}:golo-project

multipass --verbose exec ${vm_name} -- sudo -- bash <<EOF
apt install openjdk-8-jdk -y
apt install -y maven
apt install -y mercurial
EOF

multipass --verbose exec ${vm_name} -- bash <<EOF
cd golo-project
git clone https://github.com/eclipse/golo-lang.git
EOF
```

This time, you get the `golo-lang` git repository in the shared directory:

```shell
golo-project
└── golo-lang
   ├── README.md

```

Then you can edit code from the host computer, and build Golo by opening a shell on the running VM and run the `./gradlew installDist` command in the `golo-project/golo-lang/` directory. You'll find the working distribution in `golo-project/golo-lang/build/install`.
