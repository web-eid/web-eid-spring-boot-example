#!/bin/bash
#
# This script downloads and installs Web eID in .deb based Linux distributions.
# License: public domain.
# Based on https://github.com/open-eid/linux-installer/blob/master/install-open-eid.sh

set -eu

test_sudo() {
  if ! command -v sudo>/dev/null; then
     make_fail "You must have sudo and be in sudo group\nAs root do: apt-get install sudo && adduser $USER sudo"
  fi
}

test_root() {
  if test $(id -u) -eq 0; then
    echo "You run this script as root. DO NOT RUN RANDOM SCRIPTS AS ROOT."
    exit 2
  fi
}

make_fail() {
  echo -e "$1"
  exit 3
}

make_warn() {
  echo "### $1"
  echo "Press ENTER to continue, CTRL-C to cancel"
  read -r dummy
}

make_install() {
  echo "Installing Web eID packages for Ubuntu $1"
  TMPDIR=`mktemp -d`
  cd $TMPDIR
  VERSION='2.2.0'
  # BUILD=`[[ $1 == *0 ]] && echo 555 || echo 552`
  BUILD='572'
  UBUNTU_VERSION=${1//./}
  wget "https://installer.id.ee/media/web-eid/Ubuntu/web-eid_${VERSION}.${BUILD}-${UBUNTU_VERSION}_all.deb"
  wget "https://installer.id.ee/media/web-eid/Ubuntu/web-eid-chrome_${VERSION}.${BUILD}-${UBUNTU_VERSION}_all.deb"
  wget "https://installer.id.ee/media/web-eid/Ubuntu/web-eid-firefox_${VERSION}.${BUILD}-${UBUNTU_VERSION}_all.deb"
  wget "https://installer.id.ee/media/web-eid/Ubuntu/web-eid-native_${VERSION}.${BUILD}-${UBUNTU_VERSION}_amd64.deb"
  sudo apt install -y ./web-eid*.deb
  cd /tmp
  rm -r $TMPDIR
}

### main

# Check for Debian derivative.
if ! command -v lsb_release>/dev/null; then
  make_fail "# Not a Debian Linux derivative, cannot continue."
fi

# We use sudo.
test_root
test_sudo

# version   name    LTS   supported until
# 20.04     focal   LTS   2025-04
# 22.04     jammy   LTS   2027-04
# 22.10     kinetic   -   2023-07
LATEST_SUPPORTED_UBUNTU_CODENAME='kinetic'
LATEST_SUPPORTED_UBUNTU_VERSION='22.10'

# Check the distro and release.
distro=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
release=$(lsb_release -rs)
codename=$(lsb_release -cs)

case $distro in
   debian)
      make_warn "Debian is not officially supported"
      case "$codename" in
        bullseye)
          make_warn "Debian $codename is not officially supported"
          make_warn "Installing from ubuntu-focal repository"
          make_install '20.04'
          ;;
        *)
          make_fail "Debian $codename is not officially supported"
          ;;
      esac
      ;;
   ubuntu|neon)
      case $distro in
         neon) make_warn "Neon is not officially supported; assuming that it is equivalent to Ubuntu" ;;
         *) ;;
      esac
      case $codename in
        utopic|vivid|wily|trusty|artful|cosmic|disco|xenial|eoan|groovy|hirsute|impish|bionic)
          make_fail "Ubuntu $codename is not officially supported"
          ;;
        focal|jammy|kinetic)
          make_install $release
          ;;
        *)
          make_warn "Ubuntu $codename is not officially supported"
          make_warn "Trying to install package for Ubuntu ${LATEST_SUPPORTED_UBUNTU_CODENAME}"
          make_install ${LATEST_SUPPORTED_UBUNTU_VERSION}
          ;;
      esac
      ;;
   linuxmint)
      case $release in
        21*)
          make_warn "Linux Mint 21 is not officially supported"
          make_install '22.04'
          ;;
        20*)
          make_warn "Linux Mint 20 is not officially supported"
          make_install '20.04'
          ;;
        *)
          make_fail "Linux Mint $release is not officially supported"
          ;;
      esac
      ;;
   elementary*os|elementary)
      case $release in
        7*)
          make_warn "Elementary OS 7 is not officially supported"
          make_install '22.04'
          ;;
        *)
          make_fail "Elementary OS $release is not officially supported"
          ;;
      esac
      ;;
   pop)
      case $codename in
        artful|cosmic|disco|eoan|bionic)
          make_fail "Pop!_OS $codename is not officially supported"
          ;;
        focal)
          make_warn "Pop!_OS $codename is not officially supported"
          make_install $release
          ;;
        *)
          make_warn "Pop!_OS $codename is not officially supported"
          make_warn "Trying to install package for Pop!_OS ${LATEST_SUPPORTED_UBUNTU_CODENAME}"
          make_install ${LATEST_SUPPORTED_UBUNTU_VERSION}
          ;;
      esac
      ;;
   *)
      make_fail "$distro is not supported :("
      ;;
esac
