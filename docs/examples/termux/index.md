+++
  title = 'Integration with Termux'
+++

# Termux

&mu;Launcher has no special support for [Termux](https://termux.dev/).
However it is possible to run Termux commands from &mu;Launcher by using [Termux:Widget](https://wiki.termux.com/wiki/Termux:Widget) to create a pinned shortcut and bind that to a gesture.

* Install Termux:Widget.
* Make sure that &mu;Launcher is set as the default home screen.[^1]
* Put the script you want to run into `~/.shortcuts/`.
* Run `am start com.termux.widget/com.termux.widget.TermuxCreateShortcutActivity`. This will create a pinned shortcut which is treated like an app by &mu;Launcher, i.e. open &mu;Launcher's activity to create a shortcut.

<img src="./screenshot1.png"
     alt="screenshot"
     width="200" height="400">
<img src="./screenshot2.png"
     alt="screenshot"
     width="200" height="400">


[^1]: Only the default home screen can access shortcuts.
