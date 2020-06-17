package com.marinabragatti.obebecresceu.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {
    private static DatabaseReference firebaseRef;
    private static FirebaseAuth firebaseAuth;
    private static StorageReference storageReference;

    public static DatabaseReference getFirebaseRef(){
        if(firebaseRef == null){
            firebaseRef = FirebaseDatabase.getInstance().getReference();
        }
        return firebaseRef;
    }

    public static FirebaseAuth getFirebaseAuth(){
        if(firebaseAuth == null){
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    public static StorageReference getStorageReference(){
        if(storageReference == null){
            storageReference = FirebaseStorage.getInstance().getReference();
        }
        return storageReference;
    }

    public static String getIdUser(){
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        return autenticacao.getCurrentUser().getUid();
    }

    public static DatabaseReference recuperarUsuario(){
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();//Chamei os métodos acima para instanciar o usuario
        firebaseRef = ConfiguracaoFirebase.getFirebaseRef();//Chamei os métodos acima para instanciar dataBase
        String idUser = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                .child(idUser);

        return usuarioRef; //Retorno o id do usuário no firebase
    }
}
