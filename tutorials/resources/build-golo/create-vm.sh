#!/bin/bash

vm_name="golo-lang-vm"
vm_cpus=3
vm_mem="4G"
vm_disk="100GB"

if [ ! -d "golo-distribution" ];then
  mkdir golo-distribution
fi

multipass launch focal --name ${vm_name} --cpus ${vm_cpus} --mem ${vm_mem} --disk ${vm_disk} \
  --cloud-init ./cloud-init.yaml

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
