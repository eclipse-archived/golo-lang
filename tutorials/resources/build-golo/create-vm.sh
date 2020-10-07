#!/bin/bash

vm_name="golo-lang-vm"
vm_cpus=3
vm_mem="4G"
vm_disk="100GB"

multipass launch focal --name ${vm_name} --cpus ${vm_cpus} --mem ${vm_mem} --disk ${vm_disk} \
  --cloud-init ./cloud-init.yaml || error_code=$?

if [ "${error_code}" -eq 2 ] || [ "${error_code}" -eq 1 ]; then
  # exit code == 1: "error: No such file: ./cloud-init.yaml"
  # exit code == 2: ""launch failed: instance "golo-lang-vm" already exists
  exit 1
fi

if [ ! -d "golo-distribution" ];then
  mkdir golo-distribution
fi

echo "Initialize ${vm_name}..."

multipass mount golo-distribution ${vm_name}:golo-distribution

multipass --verbose exec ${vm_name} -- sudo -- bash <<EOF
apt install openjdk-8-jdk -y
apt install -y maven
apt install -y mercurial
EOF

multipass --verbose exec ${vm_name} -- bash <<EOF
git clone https://github.com/eclipse/golo-lang.git
cd golo-lang
./gradlew installDist
EOF

multipass --verbose exec ${vm_name} -- bash <<EOF
cp -a golo-lang/build/install/. golo-distribution
chmod -R a+rwx golo-distribution
EOF
