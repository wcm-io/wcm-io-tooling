File file = new File( basedir, "build.log" );
assert file.exists();

String text = file.getText("utf-8");

assert text.contains("node: '16.13.1'")
assert text.contains("npm: '8.1.2'")

return true;
