# Contributing to Golo

Thank you for your interest in Golo!

You can propose contributions by sending pull requests through GitHub.

And of course you can [report issues](https://github.com/eclipse/golo-lang/issues).

## Legal considerations

Please read the [Eclipse Foundation policy on accepting contributions via Git](http://wiki.eclipse.org/Development_Resources/Contributing_via_Git).

Your contribution cannot be accepted unless you have an [Eclipse Foundation Contributor License Agreement](http://www.eclipse.org/legal/CLA.php) in place.

Here is the checklist for contributions to be _acceptable_:

1. [create an account at Eclipse](https://dev.eclipse.org/site_login/createaccount.php), and
2. add your GitHub user name in your account settings, and
3. [log into the projects portal](https://projects.eclipse.org/) and look for ["Eclipse CLA"](https://projects.eclipse.org/user/sign/cla), and
4. ensure that you _sign-off_ your Git commits, and
5. ensure that you use the _same_ email address as your Eclipse Foundation email in commits.

## Technical considerations

Again, check that your author email in commits is the same as your Eclipse Foundation account, and make sure that you sign-off every commit (`git commit -s`).

Do not make pull requests from your `master` branch, please use topic branches instead.

When submitting code, please make every effort to follow existing conventions and style in order to
keep the code as readable as possible.

Please provide meaningful commit messages. You can take inspiration from
[http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).

Do not forget to mention the related Eclipse Bugzilla issue, if any.

Here is a sample _good_ Git commit log message:

    [666999] Quick summary

    This is a discussion of the change with details on the impact, limitations, etc.

    Write just like if you were discussing with fellows :-)

    Bug: https://github.com/eclipse/golo-lang/issues/69
    Also-By: Somebody Who Contributed <foo@bar.com>
    Signed-off-by: Another Person <baz@foobar.org>

Finally, a contribution is not a good contribution unless it comes with unit tests, integration tests and
documentation.

## Misc.

### Checklist when adding a core language feature

- Update the diagnosis visitors if applicable: ir and ast
- Update the golodoc visitors and templates if applicable: ctags, html and
  markdown
- Update the main language documentation
- Add tests
- Add sample files
- Update the completion scripts if applicable
- Update highlighters and IDE plugins
