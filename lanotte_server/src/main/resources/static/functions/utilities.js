
function allowedCity() {
    let city = document.getElementById('city');
    let CAP = document.getElementById('CAP');
    var allowedCity= Boolean(true);
    var presentedAlert = Boolean(false);

    if (city.value.toLowerCase() !== "l'aquila"){
        alert("Al momento il servizio è attivo solamente nella città di L'Aquila.");
        allowedCity = false;
        presentedAlert = true;
    }

    if (CAP.value !== "67100" && !presentedAlert) {
        alert("Al momento il servizio è attivo solamente nella città di L'Aquila.");
        allowedCity = false;
    }
    return allowedCity;
}

function passwordCheck(){
    let pass1 = document.getElementById('pass1').value;
    let pass2 = document.getElementById('pass2').value;
    var passwordCheck = Boolean(true);

    console.log(pass1);
    console.log(pass2);

    if (pass1 !== pass2) {
        alert("Le due password non combaciano. Reinserirle correttamente.");
        passwordCheck = false;
    }

    return passwordCheck;
}

// https://gist.github.com/mahizsas/4347946
function controllaPIVA() {
    let pi = document.getElementById("vat_number").value;
    console.log(pi);
    console.log(pi.length);

    // La lunghezza della partita IVA non è corretta: la partita IVA dovrebbe essere lunga esattamente 11 caratteri
    if( pi.length !== 11 ) {
        alert("Lunghezza partita IVA non valida. Deve essere lunga esattamente 11 caratteri.");
        return false;
    }

    validi = "0123456789";
    // la piva contiene un carattere non valido. I caratteri validi sono solo le cifre
    for(let i = 0; i < 11; i++ ){
        if( validi.indexOf( pi.charAt(i) ) === -1 ) {
            alert("La Partita IVA contiene caratteri non validi. I caratteri validi sono solo le cifre.");
            return false;
        }

    }
    let s = 0;
    for(let i = 0; i <= 9; i += 2 ) {
        s += pi.charCodeAt(i) - '0'.charCodeAt(0);
    }

    for(let i = 1; i <= 9; i += 2 ){
        let c = 2*( pi.charCodeAt(i) - '0'.charCodeAt(0) );
        if( c > 9 ) {
            c = c - 9;
        }
        s += c;
    }

    // il codice di controllo non corrisponde
    if( ( 10 - s%10 )%10 !== pi.charCodeAt(10) - '0'.charCodeAt(0) ) {
        alert("Il codice di controllo della partita IVA non corrisponde.");
        return false;
    }

    return true;
}

function validateNewBusiness(){
    return !( !controllaPIVA() || !allowedCity() || !passwordCheck() );
}

var i = 0;

function addIngredientField(){
    var container = document.getElementById("ingredientsContainer");
    var clone = container.cloneNode(true);
    container.parentNode.appendChild(clone);
}

function addIngredientFieldMod(){
    var container = document.getElementById("ingredientsContainer");
    var clone = container.cloneNode(true);
    var parent = document.getElementById("parentParentNode");
   parent.appendChild(clone);
}