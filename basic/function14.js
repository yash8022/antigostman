function SayMyName(){
    console.log("Y")
    console.log("a")
    console.log("s")
    console.log("h")

}
// SayMyName()

function addTwoNumber(number1,number2){//parameters
    let Result=number1 +number2
    return Result;
}

const Result= addTwoNumber(2,6)

// console.log("Result =",Result);

function JustLoggedIn(username){
    if(username==undefined){
        console.log("Please Enter a Username");
        return ;

    }
    return`${username} Just logged in`
}

console.log(JustLoggedIn("Yash Pal Singh"))
console.log(JustLoggedIn(""))
console.log(JustLoggedIn())



